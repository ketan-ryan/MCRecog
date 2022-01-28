package com.mco.mcrecog;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.data.advancements.TheEndAdvancements;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
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
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
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
    // Scuffed and hacky way to sync data
    private int beneficence = 0;
    // Duration of beneficence timer
    private int currentCap = 0;
    // How long to disable effects for
    private int effectTimer = 0;
    // Max duration effects can be disabled
    private static final int DISABLED_TIME = 800;
    // Time before randomizing words after unlocking the end achievement
    private static final int RANDOM_TIME = 2400;
    private boolean endAdvDone;

    public McRecog() {
        MCRConfig.register(ModLoadingContext.get());
        // Connect to the server
        try {
            server = new ServerSocket(7777);
        } catch (IOException e){
            LOGGER.error(e);
        }

        // Spawn a new thread that reads from the socket on the specified localhost:port and adds it to the blocking queue
        new Thread(() -> {
            try {
                Socket client = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                // Receive input while the program is running
                while (true) {
                    String fromClient = in.readLine();
                    if(fromClient != null)
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
        new MCRGui(Minecraft.getInstance());
    }

    /**
     * Runs on every tick, but we only care about the server-side ticks
     * Retrieves a string from the blocking queue, parses it, and performs the corresponding action
     * @param event PlayerTickEvent
     */
    @SubscribeEvent
    public void onTickEvent(TickEvent.PlayerTickEvent event) {
        // Almost all of our events need to take place on the server side
        if(!event.player.level.isClientSide()) {
            // Get the string off the queue
            String msg;
            while ((msg = queue.poll()) != null) {
                if(msg.contains("STATS"))
                    displayStat(event.player, msg);
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
        // Sounds play on the client side
        else {
            String msg;
            while((msg = clientQueue.poll()) != null) {
                if (msg.contains("Update beneficence")) {
                    int i = Integer.parseInt(String.valueOf(Arrays.copyOfRange(msg.toCharArray(), "Update beneficence".length(), msg.length())).strip());
                    this.currentCap = i;
                    if (event.player instanceof LocalPlayer p)
                        p.getPersistentData().putInt("beneficence", i);
                }
                if (msg.equals("No effects")) {
                    if(event.player instanceof LocalPlayer p)
                        p.getPersistentData().putInt("disabled", DISABLED_TIME);
                }
                if (msg.equals("Play dragon noise"))
                    event.player.playSound(SoundEvents.ENDER_DRAGON_GROWL, 10.0F, 1.0F);
                if (msg.equals("Ink Splat")) {
                    event.player.playSound(SoundEvents.SLIME_JUMP, 10.0F, 1.0F);
                    if (event.player instanceof LocalPlayer p) {
                        p.getPersistentData().putInt("splatTicks", SPLAT_TICKS);
                        p.getPersistentData().putInt("splatStart", SPLAT_START);
                    }
                }
                if (msg.equals("Knockback")) {
                    int i = rand.nextInt(5) + 2;
                    int randX = rand.nextDouble() < 0.5 ? -1 : 1;
                    Player player = event.player;
                    player.knockback(((float)i * 0.5F), Mth.sin(player.getYRot() * ((float)Math.PI / 180F)) * randX,
                            (-Mth.cos(player.getYRot() * ((float)Math.PI / 180F))));
                }
            }
        }

        // If player has unlocked the end achievement, shuffle words approx every 60s
        if(event.player.getLevel() instanceof ServerLevel sl) {
            var adv = sl.getServer().getServerResources().getAdvancements().getAdvancement(new ResourceLocation("minecraft:story/enter_the_end"));
            if(event.player instanceof ServerPlayer sp) {
                if(adv != null)
                   endAdvDone = sp.getAdvancements().getOrStartProgress(adv).isDone();
            }
        }

        if (event.player instanceof LocalPlayer p) {
            // Update beneficence
            int b = p.getPersistentData().getInt("beneficence");
            if (b > 0)
                p.getPersistentData().putInt("beneficence", b - 1);
            this.beneficence = p.getPersistentData().getInt("beneficence");
            // Update disabled timer
            int d = p.getPersistentData().getInt("disabled");
            if (d > 0)
                p.getPersistentData().putInt("disabled", d - 1);
            this.effectTimer = p.getPersistentData().getInt("disabled");
            // Update random timer
            int r = p.getPersistentData().getInt("random");
            if (r > 0)
                p.getPersistentData().putInt("random", r - 1);
            else if (endAdvDone) {
                p.getPersistentData().putInt("random", RANDOM_TIME);
                Collections.shuffle(RESPONSES);
                System.out.println("Shuffling List");
                if (MCRConfig.COMMON.debugLevel.get() >= 1)
                    p.sendMessage(new TextComponent("Words have been shuffled"), Util.NIL_UUID);
            }
            // Ink splat
            if(p.getPersistentData().getInt("splatStart") > 0)
                p.getPersistentData().putInt("splatStart", p.getPersistentData().getInt("splatStart") - 1);

            if(p.getPersistentData().getInt("splatStart") == 0 && p.getPersistentData().getInt("splatTicks") > 0)
                p.getPersistentData().putInt("splatTicks", p.getPersistentData().getInt("splatTicks") - 1);
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
        if (players.size() > 0 && players.get(0) != null) {
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
        float scale = BigDecimal.valueOf(MCRConfig.COMMON.deathCountScale.get()).floatValue();
        stack.scale(scale, scale, scale);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        //        stack, str,                  x,                      y,                color
        font.drawShadow(stack, overlayMessageString, (float)(-l / 2), -20.0F, 16777215);
        RenderSystem.disableBlend();
        stack.popPose();

        var data = minecraft.player.getPersistentData();
        MCRGui.renderBar("cooldownBar", "beneficence", currentCap, 105, 0F, 1F, 0.25F);
        int disabledYOff = 105;
        if(data.getInt("beneficence") > 0) {
            if(data.getInt("disabled") > 0)
                disabledYOff = 100;
        }
        MCRGui.renderBar("disabledBar", "disabled", DISABLED_TIME, disabledYOff, 1F, 0F, 0.168F);
        int randomYOff = 105;
        if(data.getInt("beneficence") > 0) {
            if (data.getInt("disabled") > 0)
                randomYOff = 95;
            else randomYOff = 100;
        }
        MCRGui.renderBar("randomBar", "random", RANDOM_TIME, randomYOff, 0F, 0.717F, 1F);
    }

    @SubscribeEvent
    public void onAchievementGet(AdvancementEvent event) {
        // Shuffle words when entering the end or nether
        String adv = event.getAdvancement().getId().toString();
        // Shuffle triggers when entering the end or nether
        if(adv.equals("minecraft:story/enter_the_nether") || adv.equals("minecraft:story/enter_the_end")) {
            Collections.shuffle(RESPONSES);
            System.out.println("Shuffled list");
        }

        // We only want to display stats when the game is completed
        if(adv.equals("minecraft:end/kill_dragon")) {
            List<ServerPlayer> players = Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers();
            if (!(players.size() > 0 && players.get(0) != null)) return;

            ServerPlayer sp = players.get(0);

            // Display all statistics on beating the game
            Player player = event.getPlayer();
            player.sendMessage(new TextComponent("Congratulations " + player.getDisplayName().getString() +
                    " on beating the game!")
                    .withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD), Util.NIL_UUID);
            player.sendMessage(new TextComponent("You died " + sp.getStats().getValue(Stats.CUSTOM, Stats.DEATHS) +
                    " times!").withStyle(ChatFormatting.DARK_RED), Util.NIL_UUID);
            player.sendMessage(new TextComponent("Hope you enjoyed!"), Util.NIL_UUID);
            player.sendMessage(new TextComponent("Mod made by HazeyGoldenAntlers aka TheMinecraftOverlord on YT"), Util.NIL_UUID);
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
        if (this.effectTimer > 0) return null;
        // Get the level (world) instance from the player
        Level level = player.getLevel();
        // Declare variables
        String first, word, second;
        word = "";

        for(String resp: RESPONSES)
            System.out.println(resp);

        if (peek == null)
            peek = "";
        if (RESPONSES.contains(msg))
            System.out.println(TRIGGERS.get(RESPONSES.indexOf(msg)));
        // Now we go down the possible responses
        if (RESPONSES.get(0).equals(msg) ) {
            // No Shot
            int slot = player.getInventory().findSlotMatchingItem(new ItemStack(Items.ARROW));
            if (slot != -1) {
                player.getInventory().removeItem(slot, 10);
            }
            word = TRIGGERS.get(0);
        }
        if (RESPONSES.get(1).equals(msg) ) {
            // Bear
            summonEntityOffset(player, level, EntityType.POLAR_BEAR, true, 7, null, 0, null,4);
            if(MCRConfig.COMMON.waterWhenSpawning.get() && player.isInWater())
                player.addEffect(new MobEffectInstance(MCREffects.GRAVITY.get(), 1200, 0));

            word = TRIGGERS.get(1);
        }
        if (RESPONSES.get(2).equals(msg) ) {
            // Axolotl
            // Duration      Amplifier
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 2));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
            summonEntity(player, level, EntityType.TROPICAL_FISH, false, 15, null, 0, null);

            word = TRIGGERS.get(2);
        }
        if (RESPONSES.get(3).equals(msg) ) {
            // Rot
            summonEntityOffset(player, level, EntityType.ZOMBIE, false, 7, MobEffects.MOVEMENT_SPEED,
                    rand.nextInt(2),
                    new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_SWORD)}, 2);

            word = TRIGGERS.get(3);
        }
        if (RESPONSES.get(4).equals(msg) ) {
            // Bone
            summonEntity(player, level, EntityType.SKELETON, false, 7, MobEffects.MOVEMENT_SPEED,
                    rand.nextInt(2),
                    new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.STONE_SWORD)});

            word = TRIGGERS.get(4);
        }
        if (RESPONSES.get(5).equals(msg) ) {
            // Pig
            player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 20);
            word = TRIGGERS.get(5);
        }
        if (RESPONSES.get(6).equals(msg))  {
            // Sub
            // 36 from inventory, 1 from offhand, 4 from armor
            int chances = rand.nextInt(41);
            if(chances < 36) {
                // Remove random item from inventory
                removeRandomItem(player);
            }
            else if(chances == 37) {
                // Clear offhand
                if(!player.getInventory().offhand.get(0).isEmpty())
                    player.getInventory().offhand.clear();
            } else {
                // Remove random armor piece
                boolean empty = true;
                for(ItemStack stack : player.getInventory().armor) {
                    if(!stack.isEmpty())
                        empty = false;
                }
                if (empty) return null;

                int slot = rand.nextInt(4);
                while (player.getInventory().armor.get(slot).equals(ItemStack.EMPTY))
                    slot = rand.nextInt(4);

                player.getInventory().armor.get(slot).shrink(1);
            }

            word = TRIGGERS.get(6);
        }
        if (RESPONSES.get(7).equals(msg) ) {
            // Creep
            summonEntityOffset(player, level, EntityType.CREEPER, false, 7, null, 0, null, 2);
            if(MCRConfig.COMMON.waterWhenSpawning.get() && player.isInWater())
                player.addEffect(new MobEffectInstance(MCREffects.GRAVITY.get(), 1200, 0));

            word = TRIGGERS.get(7);
        }
        if (RESPONSES.get(8).equals(msg) ) {
            // Rod
            summonEntity(player, level, EntityType.BLAZE, false, 7, null, 0, null);
            if(MCRConfig.COMMON.waterWhenSpawning.get() && player.isInWater())
                player.addEffect(new MobEffectInstance(MCREffects.GRAVITY.get(), 1200, 0));

            word = TRIGGERS.get(8);
        }
        if (RESPONSES.get(9).equals(msg) ) {
            // End
            clearBlocksAbove(player, level);
            summonEntity(player, level, EntityType.ENDERMAN, true, 7, null, 0, null);
            if(MCRConfig.COMMON.waterWhenSpawning.get() && player.isInWater())
                player.addEffect(new MobEffectInstance(MCREffects.GRAVITY.get(), 1200, 0));

            word = TRIGGERS.get(9);
        }
        if (RESPONSES.get(10).equals(msg) ) {
            // Nether
            clearBlocksAbove(player, level);
            summonEntity(player, level, EntityType.WITHER_SKELETON, false, 7, MobEffects.MOVEMENT_SPEED, 2,
                    new ItemStack[]{new ItemStack(Items.GOLDEN_SWORD)});

            word = TRIGGERS.get(10);
        }
        if (RESPONSES.get(11).equals(msg))  {
            // Cave
            // A minute of mining fatigue
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 600, 4));
            word = TRIGGERS.get(11);
        }

        if (RESPONSES.get(12).equals(msg))  {
            // Follow
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
            word = TRIGGERS.get(12);
        }
        if (RESPONSES.get(13).equals(msg))  {
            // Day
            ServerLevel l;
            if (level instanceof ServerLevel) {
                l = (ServerLevel) level;
                l.setDayTime(20000);
            }
            word = TRIGGERS.get(13);
        }
        if (RESPONSES.get(14).equals(msg))  {
            // Bed
            summonEntity(player, level, EntityType.PHANTOM, false, 7, MobEffects.DAMAGE_RESISTANCE,
                    2, null);

            word = TRIGGERS.get(14);
        }
        if (RESPONSES.get(15).equals(msg))  {
            // Dragon
            summonEntity(player, level, EntityType.ENDERMITE, false, 10, null, 0, null);

            clientQueue.add("Play dragon noise");
            word = TRIGGERS.get(15);
        }
        if (RESPONSES.get(16).equals(msg))  {
            // Twitch
            Creeper creeper = EntityType.CREEPER.create(level);
            if (creeper != null) {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("powered", true);
                creeper.readAdditionalSaveData(tag);
                creeper.setTarget(player);

                creeper.getPersistentData().putBoolean("dropless", true);

                creeper.setPos(player.position());
                level.addFreshEntity(creeper);
            }
            word = TRIGGERS.get(16);
        }
        if (RESPONSES.get(17).equals(msg))  {
            // Coal
            player.setSecondsOnFire(100);
            player.setRemainingFireTicks(1000);
            player.setSharedFlagOnFire(true);

            word = TRIGGERS.get(17);
        }
        if (RESPONSES.get(18).equals(msg))  {
            // Iron
            summonEntity(player, level, EntityType.IRON_GOLEM, true, 1, null, 0, null);

            word = TRIGGERS.get(18);
        }
        if (RESPONSES.get(19).equals(msg))  {
            // Gold
            summonEntity(player, level, EntityType.PIGLIN_BRUTE, true, 7, null, 0,
                    new ItemStack[]{new ItemStack(Items.GOLDEN_SWORD),
                            new ItemStack(Items.GOLDEN_HELMET),
                            new ItemStack(Items.GOLDEN_CHESTPLATE)});

            word = TRIGGERS.get(19);
        }
        if (RESPONSES.get(20).equals(msg))  {
            // Diamond
            player.setHealth(1);

            word = TRIGGERS.get(20);
        }
        if (RESPONSES.get(21).equals(msg))  {
            // Mod
            Collections.shuffle(player.getInventory().items);

            word = TRIGGERS.get(21);
        }
        if (RESPONSES.get(22).equals(msg))  {
            // Port
            double d0 = player.getX();
            double d1 = player.getY();
            double d2 = player.getZ();

            double d3 = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;
            double d4 = Mth.clamp(player.getY() + (double)(player.getRandom().nextInt(16) - 8),
                    level.getMinBuildHeight(), (level.getMinBuildHeight() + ((ServerLevel)level).getLogicalHeight() - 1));
            double d5 = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;

            if(player instanceof ServerPlayer sp) {
                net.minecraftforge.event.entity.EntityTeleportEvent.ChorusFruit event =
                        net.minecraftforge.event.ForgeEventFactory.onChorusFruitTeleport(sp, d3, d4, d5);
                if (sp.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
                    SoundEvent soundevent = SoundEvents.CHORUS_FRUIT_TELEPORT;
                    level.playSound(null, d0, d1, d2, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            } else queue.add(msg);
            word = TRIGGERS.get(22);
        }
        if (RESPONSES.get(23).equals(msg))  {
            // Water
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, rand.nextInt(1200),
                    rand.nextInt(3)));
            player.addEffect(new MobEffectInstance(MCREffects.GRAVITY.get(), 1200, 0));

            word = TRIGGERS.get(23);
        }
        if (RESPONSES.get(24).equals(msg))  {
            // Block
            for(int i = 0; i < 7; i++) {
                Rabbit rabbit = EntityType.RABBIT.create(level);
                if (rabbit != null) {
                    rabbit.setRabbitType(99);
                    rabbit.setPos(player.position().add(rand.nextInt(3), 1, rand.nextInt(3)));
                    rabbit.getPersistentData().putBoolean("dropless", true);
                    level.addFreshEntity(rabbit);
                }
            }
            word = TRIGGERS.get(24);
        }
        if (RESPONSES.get(25).equals(msg))  {
            // High
            int height = 100;
            BlockPos pos = player.blockPosition().offset(0, height, 0);

            if(!player.isInWater() && !level.dimensionType().hasCeiling()) {
                while (!level.getBlockState(pos).equals(Blocks.AIR.defaultBlockState())) {
                    height += 100;
                    pos = player.blockPosition().offset(0, height, 0);
                }
                player.moveTo(pos.getX(), pos.getY(), pos.getZ());
                word = TRIGGERS.get(25);
            }
        }
        if (RESPONSES.get(26).equals(msg))  {
            // Craft
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

            word = TRIGGERS.get(26);
        }
        if (RESPONSES.get(27).equals(msg))  {
            // Village
            summonEntity(player, level, EntityType.WITCH, false, 4, MobEffects.INVISIBILITY, 0, null);
            word = TRIGGERS.get(27);
        }
        if (RESPONSES.get(28).equals(msg))  {
            // Mine
            Item randItem = USELESS_ITEMS.get(rand.nextInt(USELESS_ITEMS.size()));

            giveItem(player, randItem, rand.nextInt(64));
            word = TRIGGERS.get(28);
        }
        if (RESPONSES.get(29).equals(msg))  {
            // Gam
            Vec3 vec = player.position().add(randomOffset(10));
            level.explode(null, DamageSource.badRespawnPointExplosion(),  null, vec.x, vec.y,
                    vec.z, 5.0F, true, Explosion.BlockInteraction.DESTROY);
            word = TRIGGERS.get(29);
        }
        if (RESPONSES.get(30).equals(msg))  {
            // Light
            summonEntityOffset(player, level, EntityType.LIGHTNING_BOLT, false, 7, null, 0, null, 10);
            word = TRIGGERS.get(30);
        }
        if (RESPONSES.get(31).equals(msg)) {
            // Ink
            clientQueue.add("Ink Splat");
            word = TRIGGERS.get(31);
        }
        if (RESPONSES.get(32).equals(msg)) {
            // Bud
            clientQueue.add("Knockback");
            word = TRIGGERS.get(32);
        }
        if (RESPONSES.get(33).equals(msg)) {
            // Yike
            level.setBlockAndUpdate(player.blockPosition(), Blocks.LAVA.defaultBlockState());
            word = TRIGGERS.get(33);
        }
        if (RESPONSES.get(34).equals(msg)) {
            // Poggers
            if(this.beneficence <= 0) {
                player.heal(2);
                clientQueue.add("Update beneficence 1200");
            }
            word = TRIGGERS.get(34);
        }
        if (RESPONSES.get(35).equals(msg)) {
            // Bless me papi
            if (this.beneficence <= 0) {
                clientQueue.add("Update beneficence 3600");
                clientQueue.add("No effects");
            }
            word = TRIGGERS.get(35);
        }
        if (RESPONSES.get(36).equals(msg)) {
            // Dream
            player.kill();
            word = TRIGGERS.get(36);
        }
        if (RESPONSES.get(37).equals(msg)) {
            // Thing
            if(this.beneficence <= 0) {
                giveItem(player, Items.IRON_NUGGET, 1);
                clientQueue.add("Update beneficence 800");
            }
            word = TRIGGERS.get(37);
        }
        if (RESPONSES.get(38).equals(msg)) {
            // godlike
            if(this.beneficence <= 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 0));
                clientQueue.add("Update beneficence 1200");
            }
            word = TRIGGERS.get(38);
        }
        if (RESPONSES.get(39).equals(msg)) {
            // Troll
            player.getInventory().dropAll();
            word = TRIGGERS.get(39);
        }

        // Format the input message by highlighting the keyword yellow
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
