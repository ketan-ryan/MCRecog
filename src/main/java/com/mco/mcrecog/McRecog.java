package com.mco.mcrecog;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("mcrecog")
public class McRecog
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    // The web socket
    private ServerSocket server;
    // The blocking (thread-safe) queue to put our input onto in order to communicate between the socket thread and the main thread
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    // Instance of random
    private final Random rand = new Random();

    public McRecog() {
        MCRConfig.register(ModLoadingContext.get());
        // Connect to the server
        try {
            server = new ServerSocket(8080);
        } catch (IOException e){
            LOGGER.error(e);
        }

        // Spawn a new thread that reads from the socket on the specified localhost:port and adds it to the blocking queue
        new Thread(() ->{
            try {
                Socket client = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                // Receive input while the program is running
                while (true) {
                    String fromClient = in.readLine();
                    queue.put(fromClient);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Runs on every tick, but we only care about the server-side ticks
     * Retrieves a string from the blocking queue, parses it, and performs the corresponding action
     * @param event PlayerTickEvent
     */
    @SubscribeEvent
    public void onTickEvent(TickEvent.PlayerTickEvent event) {
        // All of our events need to take place on the server side
        if(!event.player.level.isClientSide()) {
            // Get the string off the queue
            String msg;
            while ((msg = queue.poll()) != null) {
                Component result = parseAndHandle(msg, event.player, queue.peek());

                // Config-dependent debugging
                if(result != null && MCRConfig.COMMON.debugLevel.get() > 0) {
                    event.player.sendMessage(result, Util.NIL_UUID);
                }

                if(MCRConfig.COMMON.debugLevel.get() > 1) {
                    event.player.sendMessage( new TextComponent("Message: " + msg), Util.NIL_UUID);
                }
            }
        }
    }

    /**
     * Given a message, player instance, and the next message, determines what action to perform
     * @param msg The current action to perform
     * @param player The player to perform them on
     * @param peek The source input
     * @return A formatted MutableComponent to print in the chat
     */
    private Component parseAndHandle(String msg, Player player, String peek) {
        // Get the level (world) instance from the player
        Level level = player.getLevel();
        // Declare variables
        String first, word, second;
        word = "";

        if (peek == null)
            peek = "";

        // Now we go down the cases
        switch (msg) {
            case "Lose 5 arrows" -> {
                int slot = player.getInventory().findSlotMatchingItem(new ItemStack(Items.ARROW));
                if (slot != -1) {
                    player.getInventory().removeItem(slot, 5);
                }
                word = "no shot";
            }
            case "Spawn 5 hostile polar bears" -> {
                for (int i = 0; i < 5; i++) {
                    PolarBear bear = new PolarBear(EntityType.POLAR_BEAR, level);
                    bear.setPersistentAngerTarget(player.getUUID());
                    bear.setPos(player.position().add(0, 1, 0));
                    level.addFreshEntity(bear);
                }
                word = "bear";
            }
            case "Axolotl time" -> {
                // Duration      Amplifier
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 2));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
                for (int i = 0; i < 15; i++) {
                    TropicalFish fish = new TropicalFish(EntityType.TROPICAL_FISH, level);
                    fish.setPos(player.position().add(0, 1, 0));
                    level.addFreshEntity(fish);
                }
                word = "axolotl";
            }
            case "Spawn 7 zombies" -> {
                for (int i = 0; i < 7; i++) {
                    Zombie zombie = EntityType.ZOMBIE.create(level);
                    if (zombie != null) {
                        zombie.equipItemIfPossible(new ItemStack(Items.GOLDEN_HELMET));
                        zombie.setPos(player.blockPosition().getX() + rand.nextInt(3),
                                player.blockPosition().getY(),
                                player.blockPosition().getZ() + rand.nextInt(3));
                        level.addFreshEntity(zombie);
                    }
                }
                word = "rot";
            }
            case "Spawn 7 skeletons" -> {
                for (int i = 0; i < 7; i++) {
                    Skeleton skeleton = EntityType.SKELETON.create(level);
                    if (skeleton != null) {
                        skeleton.equipItemIfPossible(new ItemStack(Items.IRON_HELMET));
                        skeleton.equipItemIfPossible(new ItemStack(Items.BOW));
                        skeleton.setPos(player.position().add(rand.nextInt(3), 1, rand.nextInt(3)));
                        level.addFreshEntity(skeleton);
                    }
                }
                word = "bone";
            }
            case "Spawn 5 creepers" -> {
                for (int i = 0; i < 5; i++) {
                    Creeper creeper = EntityType.CREEPER.create(level);
                    if (creeper != null) {
                        creeper.setPos(player.position().add(rand.nextInt(3), 1, rand.nextInt(3)));
                        level.addFreshEntity(creeper);
                    }
                }
                word = "creep";
            }
            case "Spawn 5 blazes" -> {
                for (int i = 0; i < 5; i++) {
                    Blaze blaze = EntityType.BLAZE.create(level);
                    if (blaze != null) {
                        blaze.setPos(player.position().add(rand.nextInt(3), 1, rand.nextInt(3)));
                        level.addFreshEntity(blaze);
                    }
                }
                word = "rod";
            }
            case "Spawn 7 wither skeletons" -> {
                for (int i = 0; i < 7; i++) {
                    WitherSkeleton witherSkeleton = EntityType.WITHER_SKELETON.create(level);
                    if (witherSkeleton != null) {
                        witherSkeleton.setPos(player.position().add(rand.nextInt(3), 1, rand.nextInt(3)));
                        level.addFreshEntity(witherSkeleton);
                    }
                }
                word = "nether";
            }
            case "Spawn 5 angry endermen" -> {
                for (int i = 0; i < 7; i++) {
                    EnderMan enderman = EntityType.ENDERMAN.create(level);
                    if (enderman != null) {
                        enderman.setPersistentAngerTarget(player.getUUID());
                        enderman.setRemainingPersistentAngerTime(Integer.MAX_VALUE);
                        enderman.setBeingStaredAt();
                        enderman.setTarget(player);
                        enderman.setPos(player.position().add(rand.nextInt(3), 1, rand.nextInt(3)));
                        level.addFreshEntity(enderman);
                    }
                }
                word = "end";
            }
            case "Drop hunger by 5" -> {
                player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 10);
                word = "pig";
            }
            case "Mining fatigue" -> {
                // A minute of mining fatigue
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1200, 1));
                word = "cave";
            }
            case "Lose something random" -> {
                // 36 from inventory, 1 from offhand, 4 from armor
                int chances = rand.nextInt(41);
                if(chances < 36) {
                    // Remove random item from inventory
                    removeRandomItem(player);
                }
                else if(chances == 37) {
                    // Clear offhand
                    if(!player.getInventory().offhand.get(0).equals(ItemStack.EMPTY))
                        player.getInventory().offhand.clear();
                } else {
                    // Remove random armor piece
                    int slot = rand.nextInt(4);
                    while (player.getInventory().armor.get(slot).equals(ItemStack.EMPTY))
                        slot = rand.nextInt(4);

                    player.getInventory().armor.get(slot).shrink(1);
                }

                word = "sub";
            }
            case "8 block hole" -> {
                BlockPos pos = player.blockPosition();
                for(int i = 1; i < 8; i++) {
                    level.setBlock(pos.north().offset(0, -i, 0), Blocks.AIR.defaultBlockState(), 2);
                    level.setBlock(pos.west().offset(0, -i, 0), Blocks.AIR.defaultBlockState(), 2);
                    level.setBlock(pos.south().offset(0, -i, 0), Blocks.AIR.defaultBlockState(), 2);
                    level.setBlock(pos.east().offset(0, -i, 0), Blocks.AIR.defaultBlockState(), 2);

                    level.setBlock(pos.offset(0, -i, 0), Blocks.AIR.defaultBlockState(), 2);

                    level.setBlock(pos.east().north().offset(0, -i, 0), Blocks.AIR.defaultBlockState(), 2);
                    level.setBlock(pos.east().south().offset(0, -i, 0), Blocks.AIR.defaultBlockState(), 2);
                    level.setBlock(pos.west().north().offset(0, -i, 0), Blocks.AIR.defaultBlockState(), 2);
                    level.setBlock(pos.west().south().offset(0, -i, 0), Blocks.AIR.defaultBlockState(), 2);
                }
                word = "follow";
            }
            case "Set time to night" -> {
                ServerLevel l;
                if (level instanceof ServerLevel) {
                    l = (ServerLevel) level;
                    l.setDayTime(20000);
                }
                word = "day";
            }
        }

        // If we have raw input and the word is in the raw input
        if(!peek.equals("") && !word.equals("") && peek.contains(word)) {
            // From the start of the string to the start of the word
            first = String.valueOf(Arrays.copyOfRange(peek.toCharArray(), 0, peek.toLowerCase().indexOf(word)));
            // From the start of the word to the end of the word
            String wordStr = String.valueOf(Arrays.copyOfRange(peek.toCharArray(), peek.toLowerCase().indexOf(word), peek.toLowerCase().indexOf(word) + word.length()));
            // From the end of the word to the end of the string
            second = String.valueOf(Arrays.copyOfRange(peek.toCharArray(), word.length() + peek.toLowerCase().indexOf(word), peek.length()));

            // Return new component with the word highlighted in yellow in the source string
            return new TextComponent("Message: ")
                    .append(new TextComponent(first).withStyle(ChatFormatting.WHITE)
                    .append(new TextComponent(wordStr).withStyle(ChatFormatting.YELLOW))
                    .append(new TextComponent(second).withStyle(ChatFormatting.WHITE)));
        }
        // If the word was not found or the input was not a command, return null
        return null;
    }

    /**
     * Removes a random amount of a random, non-empty item in the player's inventory
     * @param player the player to remove items from
     */
    void removeRandomItem(Player player) {
        if (player.getInventory().items.size() == 0)
            return;

        int slotId = rand.nextInt(player.getInventory().items.size());

        while(player.getInventory().getItem(slotId).equals(ItemStack.EMPTY))
            slotId = rand.nextInt(player.getInventory().items.size());

        int slotCount = player.getInventory().getItem(slotId).getCount() + 1;
        int c = rand.nextInt(slotCount + 1);

        System.out.println("Removing " + c + " of item " + player.getInventory().getItem(slotId));
        player.getInventory().removeItem(slotId, c);
    }

}
