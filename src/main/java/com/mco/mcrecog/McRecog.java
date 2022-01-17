package com.mco.mcrecog;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("mcrecog")
public class McRecog
{
    public static final String MODID = "mcrecog";
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
            server = new ServerSocket(7777);
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
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MCREffects.initialise(modEventBus);
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

    @SubscribeEvent
    public void onJumpEvent(LivingEvent.LivingJumpEvent event) {
        if(event.getEntityLiving().hasEffect(MCREffects.GRAVITY.get()))
            event.setResult(Event.Result.DENY);
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
            case "Lose 10 arrows" -> {
                int slot = player.getInventory().findSlotMatchingItem(new ItemStack(Items.ARROW));
                if (slot != -1) {
                    player.getInventory().removeItem(slot, 10);
                }
                word = "no shot";
            }
            case "Spawn 7 hostile polar bears" -> {
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
                        zombie.equipItemIfPossible(new ItemStack(Items.LEATHER_HELMET));
                        zombie.setPos(player.blockPosition().getX() + rand.nextInt(3),
                                player.blockPosition().getY(),
                                player.blockPosition().getZ() + rand.nextInt(3));
                        zombie.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, rand.nextInt(6)));
                        level.addFreshEntity(zombie);
                    }
                }
                word = "rot";
            }
            case "Spawn 7 skeletons" -> {
                for (int i = 0; i < 7; i++) {
                    Skeleton skeleton = EntityType.SKELETON.create(level);
                    if (skeleton != null) {
                        skeleton.equipItemIfPossible(new ItemStack(Items.LEATHER_HELMET));
                        skeleton.equipItemIfPossible(new ItemStack(Items.IRON_SWORD));
                        skeleton.setPos(player.position().add(rand.nextInt(3), 1, rand.nextInt(3)));
                        skeleton.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, rand.nextInt(6)));
                        level.addFreshEntity(skeleton);
                    }
                }
                word = "bone";
            }
            case "Spawn 7 creepers" -> {
                for (int i = 0; i < 5; i++) {
                    Creeper creeper = EntityType.CREEPER.create(level);
                    if (creeper != null) {
                        creeper.setPos(player.position().add(rand.nextInt(3), 1, rand.nextInt(3)));
                        level.addFreshEntity(creeper);
                    }
                }
                word = "creep";
            }
            case "Spawn 7 blazes" -> {
                for (int i = 0; i < 7; i++) {
                    Blaze blaze = EntityType.BLAZE.create(level);
                    if (blaze != null) {
                        blaze.setPos(player.position().add(rand.nextInt(3), 1, rand.nextInt(3)));
                        level.addFreshEntity(blaze);
                    }
                }
                word = "rod";
            }
            case "Spawn 7 wither skeletons" -> {
                clearBlocksAbove(player, level);
                for (int i = 0; i < 7; i++) {
                    WitherSkeleton witherSkeleton = EntityType.WITHER_SKELETON.create(level);
                    if (witherSkeleton != null) {
                        witherSkeleton.setPos(player.position().add(rand.nextInt(2), 0, rand.nextInt(2)));
                        witherSkeleton.equipItemIfPossible(new ItemStack(Items.STONE_SWORD));
                        witherSkeleton.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 2));
                        level.addFreshEntity(witherSkeleton);
                    }
                }
                word = "nether";
            }
            case "Spawn 7 angry endermen" -> {
                clearBlocksAbove(player, level);
                for (int i = 0; i < 7; i++) {
                    EnderMan enderman = EntityType.ENDERMAN.create(level);
                    if (enderman != null) {
                        enderman.setPersistentAngerTarget(player.getUUID());
                        enderman.setRemainingPersistentAngerTime(Integer.MAX_VALUE);
                        enderman.setBeingStaredAt();
                        enderman.setTarget(player);
                        enderman.setPos(player.position().add(rand.nextInt(2), 1, rand.nextInt(2)));
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
            case "Big hole" -> {
                BlockPos pos = player.blockPosition();
                for(int i = 1; i < 22; i++) {
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
            case "Set to half a heart" -> {
                player.setHealth(1);

                word = "diamond";
            }
            case "Adjust held item count" -> {
//                int chance = rand.nextInt(1000);
//                int count;
//                if(chance > 0 && chance <= 100)
//                    count = 0;
//                else if (chance > 100 && chance <= 150)
//                    count = 1;
//                else if (chance > 150 && chance <= 190)
//                    count = 2;
//                else if (chance > 190 && chance <= 210)
//                    count = 3;
//                else if (chance > 210 && chance <= 229)
//                    count = 4;
//                else if (chance > 229 && chance <= 249)
//                    count = 5;
//                else if (chance > 249 && chance <= 269)
//                    count = 6;
//                else if (chance > 269 && chance <= 289)
//                    count = 7;
//                else if (chance > 289 && chance <= 309)
//                    count = 8;
//                else if (chance > 309 && chance <= 329)
//                    count = 9;
//                else if (chance > 329 && chance <= 349)
//                    count = 10;
//                else if (chance > 349 && chance <= 369)
//                    count = 11;
//                else if (chance > 369 && chance <= 389)
//                    count = 12;
//                else if (chance > 389 && chance <= 409)
//                    count = 13;
//                else if (chance > 409 && chance <= 424)
//                    count = 14;
//                else if (chance > 424 && chance <= 439)
//                    count = 15;
//                else if (chance > 439 && chance <= 444)
//                    count = 16;
//                else if (chance > 444 && chance <= 459)
//                    count = 17;
//                else if (chance > 459 && chance <= 474)
//                    count = 18;
//                else if (chance > 474 && chance <= 489)
//                    count = 19;
//                else if (chance > 489 && chance <= 504)
//                    count = 20;
//                else if (chance > 409 && chance <= 424)
//                    count = 21;
//                else if (chance > 409 && chance <= 424)
//                    count = 22;
//                else if (chance > 409 && chance <= 424)
//                    count = 23;
//                else if (chance > 409 && chance <= 424)
//                    count = 24;
//                else if (chance > 409 && chance <= 424)
//                    count = 25;
//                else if (chance > 409 && chance <= 424)
//                    count = 26;
//                else if (chance > 409 && chance <= 424)
//                    count = 27;
//                else if (chance > 409 && chance <= 424)
//                    count = 28;
//                else if (chance > 409 && chance <= 424)
//                    count = 29;
//                else if (chance > 409 && chance <= 424)
//                    count = 30;
//                else if (chance > 409 && chance <= 424)
//                    count = 31;
                word = "mod";
            }
            case "Set on fire" -> {
                player.setSecondsOnFire(100);
                player.setRemainingFireTicks(1000);
                player.setSharedFlagOnFire(true);

                word = "coal";
            }
            case "Spawn 7 phantoms" -> {
                for(int i = 0; i < 7; i++) {
                    Phantom phantom = EntityType.PHANTOM.create(level);
                    if(phantom != null) {
                        phantom.setPos(player.position().add(rand.nextInt(2), 1, rand.nextInt(2)));
                        phantom.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, Integer.MAX_VALUE));
                        level.addFreshEntity(phantom);
                    }
                }
                word = "bed";
            }
            case "In water" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, rand.nextInt(1200), rand.nextInt(3)));
                player.addEffect(new MobEffectInstance(MCREffects.GRAVITY.get(), 1200, 3));

                word = "water";
            }
            case "Teleport randomly" -> {
                double d0 = player.getX();
                double d1 = player.getY();
                double d2 = player.getZ();

                double d3 = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;
                double d4 = Mth.clamp(player.getY() + (double)(player.getRandom().nextInt(16) - 8), (double)level.getMinBuildHeight(), (double)(level.getMinBuildHeight() + ((ServerLevel)level).getLogicalHeight() - 1));
                double d5 = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;

                net.minecraftforge.event.entity.EntityTeleportEvent.ChorusFruit event = net.minecraftforge.event.ForgeEventFactory.onChorusFruitTeleport(player, d3, d4, d5);
                if (player.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
                    SoundEvent soundevent = SoundEvents.CHORUS_FRUIT_TELEPORT;
                    level.playSound(null, d0, d1, d2, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.playSound(soundevent, 1.0F, 1.0F);
                }

                word = "teleport";
            }
            case "Spawn killer rabbits" -> {
                for(int i = 0; i < 7; i++) {
                    Rabbit rabbit = EntityType.RABBIT.create(level);
                    if (rabbit != null) {
                        rabbit.setRabbitType(99);
                        rabbit.setPos(player.position().add(rand.nextInt(3), 1, rand.nextInt(3)));
                        level.addFreshEntity(rabbit);
                    }
                }
                word = "block";
            }
            case "Launched in the air" -> {
                int height = 100;
                BlockPos pos = player.blockPosition().offset(0, height, 0);

                while (!level.getBlockState(pos).equals(Blocks.AIR.defaultBlockState())) {
                    height += 100;
                    pos = player.blockPosition().offset(0, height, 0);
                }
                player.moveTo(pos.getX(), pos.getY(), pos.getZ());
                word = "up";
            }
            case "Spawn supercharged creeper" -> {
                Creeper creeper = EntityType.CREEPER.create(level);
                if (creeper != null) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("powered", true);
                    creeper.addAdditionalSaveData(tag);
                    creeper.setPos(player.position());
                    level.addFreshEntity(creeper);
                }
                word = "twitch";
            }
            case "Spawn aggro iron golem" -> {
                IronGolem golem = EntityType.IRON_GOLEM.create(level);
                if (golem != null) {
                    golem.setTarget(player);
                    golem.setPos(player.position());
                    level.addFreshEntity(golem);
                }
                word = "iron";
            }
            case "Surround in stone" -> {
                BlockPos pos = player.blockPosition();
                for(int i = 0; i < 3; i ++) {
                    level.setBlock(pos.north().offset(0, i, 0), Blocks.GRANITE.defaultBlockState(), 2);
                    level.setBlock(pos.west().offset(0, i, 0), Blocks.GRANITE.defaultBlockState(), 2);
                    level.setBlock(pos.south().offset(0, i, 0), Blocks.GRANITE.defaultBlockState(), 2);
                    level.setBlock(pos.east().offset(0, i, 0), Blocks.GRANITE.defaultBlockState(), 2);

                    level.setBlock(pos.east().north().offset(0, i, 0), Blocks.GRANITE.defaultBlockState(), 2);
                    level.setBlock(pos.east().south().offset(0, i, 0), Blocks.GRANITE.defaultBlockState(), 2);
                    level.setBlock(pos.west().north().offset(0, i, 0), Blocks.GRANITE.defaultBlockState(), 2);
                    level.setBlock(pos.west().south().offset(0, i, 0), Blocks.GRANITE.defaultBlockState(), 2);
                }
                level.setBlock(pos.below(), Blocks.GRANITE.defaultBlockState(), 2);
                level.setBlock(pos.offset(0, 2, 0), Blocks.GRANITE.defaultBlockState(), 2);

                word = "craft";
            }
//            case "Play dragon noise, spawn 10 endermite" -> {
//                level.playSound(player, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.MASTER ,10.0F, 1.0F);
//                player.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.0F);
//
//                for(int i = 0; i < 10; i++) {
//                    Endermite mite = EntityType.ENDERMITE.create(level);
//                    if(mite != null) {
//                        mite.setPos(player.position().add(rand.nextInt(2), 1, rand.nextInt(2)));
//                        level.addFreshEntity(mite);
//                    }
//                }
//
//                word = "dragon";
//            }
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
     * Clears a space above the player's head so tall mobs don't suffocate
     * @param player the player to clear the blocks above
     * @param level the server level instance
     */
    void clearBlocksAbove(Player player, Level level) {
        BlockPos pos = player.blockPosition();
        for(int i = 0; i < 4; i ++) {
            level.setBlock(pos.north().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.west().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.south().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.east().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.north().east().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.south().east().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.north().west().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.south().west().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
        }
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
