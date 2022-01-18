package com.mco.mcrecog;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
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
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.mco.mcrecog.MCRUtils.*;

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
    // The blocking queue for client operations
    private final BlockingQueue<String> clientQueue = new LinkedBlockingQueue<>();
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
        else {
            String msg;
            while((msg = clientQueue.poll()) != null) {
                if (msg.equals("Play dragon noise")) {
                    event.player.playSound(SoundEvents.ENDER_DRAGON_GROWL, 10.0F, 1.0F);
                }
            }
        }
    }

    @SubscribeEvent
    public void onDropsEvent(LivingDropsEvent event) {
        // We don't want our summoned mobs to drop items
        if (event.getEntity().getPersistentData().getBoolean("dropless"))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderEvent(RenderGameOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        int deaths = 0;
        // Get number of deaths from player's stats, this only will work for one player
        List<ServerPlayer> players = minecraft.getSingleplayerServer().getPlayerList().getPlayers();
        if (players.size() > 0 &&  players.get(0) != null) {
            ServerPlayer sp = players.get(0);
            deaths = sp.getStats().getValue(Stats.CUSTOM, Stats.DEATHS);
        }
        Font font = minecraft.font;
        String overlayMessageString = "Deaths: " + deaths;
        int l = font.width(overlayMessageString);

        // Draw the words on the screen
        PoseStack stack = new PoseStack();
        stack.pushPose();
        stack.translate(MCRConfig.COMMON.deathCountX.get(), MCRConfig.COMMON.deathCountY.get(), 0.0D);
        stack.scale(4.0F, 4.0F, 4.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        //        stack, str,                  x,                      y,                color
        font.drawShadow(stack, overlayMessageString, (float)(-l / 2), -20.0F, 16777215);
        RenderSystem.disableBlend();
        stack.popPose();
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
                summonEntity(player, level, EntityType.POLAR_BEAR, true, 7, null, 0, null);

                word = "bear";
            }
            case "Axolotl time" -> {
                // Duration      Amplifier
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 2));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
                summonEntity(player, level, EntityType.TROPICAL_FISH, false, 15, null, 0, null);

                word = "axolotl";
            }
            case "Spawn 7 zombies" -> {
                summonEntity(player, level, EntityType.ZOMBIE, false, 7, MobEffects.MOVEMENT_SPEED,
                        rand.nextInt(6),
                        new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_SWORD)});

                word = "rot";
            }
            case "Spawn 7 skeletons" -> {
                summonEntity(player, level, EntityType.SKELETON, false, 7, MobEffects.MOVEMENT_SPEED,
                        rand.nextInt(6),
                        new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_SWORD)});

                word = "bone";
            }
            case "Spawn 7 creepers" -> {
                summonEntity(player, level, EntityType.CREEPER, false, 7, null, 0, null);

                word = "creep";
            }
            case "Spawn 7 blazes" -> {
                summonEntity(player, level, EntityType.BLAZE, false, 7, null, 0, null);

                word = "rod";
            }
            case "Spawn 7 wither skeletons" -> {
                clearBlocksAbove(player, level);
                summonEntity(player, level, EntityType.WITHER_SKELETON, false, 7, MobEffects.MOVEMENT_SPEED, 2,
                        new ItemStack[]{new ItemStack(Items.DIAMOND_SWORD)});

                word = "nether";
            }
            case "Spawn 7 angry endermen" -> {
                clearBlocksAbove(player, level);
                summonEntity(player, level, EntityType.ENDERMAN, true, 7, null, 0, null);

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

                word = "mod";
            }
            case "Set on fire" -> {
                player.setSecondsOnFire(100);
                player.setRemainingFireTicks(1000);
                player.setSharedFlagOnFire(true);

                word = "coal";
            }
            case "Spawn 7 phantoms" -> {
                summonEntity(player, level, EntityType.PHANTOM, false, 7, MobEffects.FIRE_RESISTANCE,
                        Integer.MAX_VALUE, null);

                word = "bed";
            }
            case "In water" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, rand.nextInt(1200),
                        rand.nextInt(3)));
                player.addEffect(new MobEffectInstance(MCREffects.GRAVITY.get(), 1200, 0));

                word = "water";
            }
            case "Teleport randomly" -> {
                double d0 = player.getX();
                double d1 = player.getY();
                double d2 = player.getZ();

                double d3 = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;
                double d4 = Mth.clamp(player.getY() + (double)(player.getRandom().nextInt(16) - 8),
                        level.getMinBuildHeight(), (level.getMinBuildHeight() + ((ServerLevel)level).getLogicalHeight() - 1));
                double d5 = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;

                net.minecraftforge.event.entity.EntityTeleportEvent.ChorusFruit event =
                        net.minecraftforge.event.ForgeEventFactory.onChorusFruitTeleport(player, d3, d4, d5);
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
                        rabbit.getPersistentData().putBoolean("dropless", true);
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
                    creeper.readAdditionalSaveData(tag);

                    creeper.getPersistentData().putBoolean("dropless", true);

                    creeper.setPos(player.position());
                    level.addFreshEntity(creeper);
                }
                word = "twitch";
            }
            case "Spawn aggro iron golem" -> {
                summonEntity(player, level, EntityType.IRON_GOLEM, true, 1, null, 0, null);

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
            case "Spawn witches" -> {
                summonEntity(player, level, EntityType.WITCH, false, 4, MobEffects.INVISIBILITY, 0, null);
                word = "village";
            }
            case "Play dragon noise, spawn 10 endermite" -> {
                summonEntity(player, level, EntityType.ENDERMITE, false, 10, null, 0, null);

                clientQueue.add("Play dragon noise");
                word = "dragon";
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

}
