package com.mco.mcrecog;

import com.mco.mcrecog.network.ServerboundKeyUpdatePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.net.ServerSocket;
import java.util.Arrays;
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
    // Whether the player has unlocked the achievement for entering the end
    private boolean endAdvDone;

    public McRecog() {
        MCRConfig.register(ModLoadingContext.get());
        // Connect to the server
//        try {
//            server = new ServerSocket(7777);
//        } catch (IOException e){
//            LOGGER.error(e);
//        }

        // Spawn a new thread that reads from the socket on the specified localhost:port and adds it to the blocking queue
//        new Thread(() -> {
//            try {
//                Socket client = server.accept();
//                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//
//                // Receive input while the program is running
//                while (true) {
//                    String fromClient = in.readLine();
//                    if(fromClient != null)
//                        queue.put(fromClient);
//                }
//            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ModSetup::init);
//        MCREffects.initialise(modEventBus);
//        new MCRGui(Minecraft.getInstance());
//        MCRSounds.SOUNDS.register(modEventBus);
    }

    /**
     * Runs on every tick, but we only care about the server-side ticks
     * Retrieves a string from the blocking queue, parses it, and performs the corresponding action
     * @param event PlayerTickEvent
     */
    /*@SubscribeEvent
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
                    if(event.player instanceof LocalPlayer p) {
                        p.getPersistentData().putInt("disabled", DISABLED_TIME);
                        p.sendMessage(new TextComponent("Effects Temporarily Disabled")
                                .withStyle(ChatFormatting.DARK_RED)
                                .withStyle(ChatFormatting.BOLD), Util.NIL_UUID);
                    }
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
                if(msg.equals("Tony time")) {
                    event.player.playSound(MCRSounds.TONY.get(), 10.0F, 1.0F);
                    if(event.player instanceof LocalPlayer p)
                        p.getPersistentData().putInt("tony", 100);
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
            if (d == 1) {
                p.sendMessage(new TextComponent("Effects Reenabled")
                        .withStyle(ChatFormatting.GREEN)
                        .withStyle(ChatFormatting.BOLD), Util.NIL_UUID);
            }
            this.effectTimer = p.getPersistentData().getInt("disabled");
            // Update random timer
            int r = p.getPersistentData().getInt("random");
            if (r > 0)
                p.getPersistentData().putInt("random", r - 1);
            else if (endAdvDone && MCRConfig.COMMON.shuffleWords.get()) {
                p.getPersistentData().putInt("random", RANDOM_TIME);
                Collections.shuffle(RESPONSES);
                System.out.println("Shuffling List");
                p.sendMessage(new TextComponent("Words have been shuffled")
                        .withStyle(ChatFormatting.BOLD)
                        .withStyle(ChatFormatting.BLUE), Util.NIL_UUID);
            }
            // Update tony timer
            int t = p.getPersistentData().getInt("tony");
            if (t > 0)
                p.getPersistentData().putInt("tony", t - 1);
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

        //        stack, str,                  x,                      y,                color as a packed int
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
        if(adv.equals("minecraft:story/enter_the_nether") || adv.equals("minecraft:story/enter_the_end") && MCRConfig.COMMON.shuffleWords.get()) {
            Collections.shuffle(RESPONSES);
            System.out.println("Shuffled list");
            Minecraft.getInstance().player.sendMessage(new TextComponent("Shuffled Words")
                    .withStyle(ChatFormatting.BLUE)
                    .withStyle(ChatFormatting.BOLD), Util.NIL_UUID);
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
    }*/

    @SubscribeEvent
    public void onKeyEvent(InputEvent.KeyInputEvent event) {
        if(event.getKey() == GLFW.GLFW_KEY_P) {
            MCPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(1));
        }
        /*if (Minecraft.getInstance().getSingleplayerServer() == null) return;
        List<ServerPlayer> players = Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers();
        if (!(players.size() > 0 && players.get(0) != null)) return;
        if(event.getAction() != GLFW.GLFW_PRESS) return;
        ServerPlayer sp = players.get(0);

        switch (event.getKey()) {
            case GLFW.GLFW_KEY_X -> parseAndHandle(RESPONSES.get(0), sp, "");
            case GLFW.GLFW_KEY_R -> parseAndHandle(RESPONSES.get(1), sp, "");
            case GLFW.GLFW_KEY_B -> parseAndHandle(RESPONSES.get(2), sp, "");
            case GLFW.GLFW_KEY_Y -> parseAndHandle(RESPONSES.get(3), sp, "");
            case GLFW.GLFW_KEY_U -> parseAndHandle(RESPONSES.get(4), sp, "");
            case GLFW.GLFW_KEY_I -> parseAndHandle(RESPONSES.get(5), sp, "");
            case GLFW.GLFW_KEY_O -> parseAndHandle(RESPONSES.get(6), sp, "");
            case GLFW.GLFW_KEY_V -> parseAndHandle(RESPONSES.get(7), sp, "");
            case GLFW.GLFW_KEY_COMMA -> parseAndHandle(RESPONSES.get(8), sp, "");
            case GLFW.GLFW_KEY_K -> parseAndHandle(RESPONSES.get(9), sp, "");
            case GLFW.GLFW_KEY_J -> parseAndHandle(RESPONSES.get(10), sp, "");
            case GLFW.GLFW_KEY_H -> parseAndHandle(RESPONSES.get(11), sp, "");
            case GLFW.GLFW_KEY_G -> parseAndHandle(RESPONSES.get(12), sp, "");
            case GLFW.GLFW_KEY_Z -> parseAndHandle(RESPONSES.get(13), sp, "");
            case GLFW.GLFW_KEY_M -> parseAndHandle(RESPONSES.get(14), sp, "");
            case GLFW.GLFW_KEY_N -> parseAndHandle(RESPONSES.get(15), sp, "");

            default -> {
            }
        }*/
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

        if (peek == null)
            peek = "";

//        if(rand.nextInt(25) == 0)
//            clientQueue.add("Tony time");

        // Now we go down the possible responses
        if (RESPONSES.get(0).equals(msg) ) {
            // Food
            player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 20);
            word = TRIGGERS.get(0);
        }
        if (RESPONSES.get(1).equals(msg) ) {
            // Remove something random from hotbar
            removeRandomItem(player);
            word = TRIGGERS.get(1);
        }
        if (RESPONSES.get(2).equals(msg) ) {
            // Hole
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
            word = TRIGGERS.get(2);
        }
        if (RESPONSES.get(3).equals(msg) ) {
            // Mine
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 2400, 4));
            word = TRIGGERS.get(3);
        }
        if (RESPONSES.get(4).equals(msg) ) {
            // Lava
            level.setBlockAndUpdate(player.blockPosition(), Blocks.LAVA.defaultBlockState());
            word = TRIGGERS.get(4);
        }
        if (RESPONSES.get(5).equals(msg) ) {
            // Night
            ServerLevel l;
            if (level instanceof ServerLevel) {
                l = (ServerLevel) level;
                l.setDayTime(20000);
            }
            word = TRIGGERS.get(5);
        }
        if (RESPONSES.get(6).equals(msg))  {
            // Drop
            player.getInventory().dropAll();
            word = TRIGGERS.get(6);
        }
        if (RESPONSES.get(7).equals(msg) ) {
            // Jump
            int height = 100;
            BlockPos pos = player.blockPosition().offset(0, height, 0);

            if(!level.dimensionType().hasCeiling()) {
                while (!level.getBlockState(pos).equals(Blocks.AIR.defaultBlockState())) {
                    height += 100;
                    pos = player.blockPosition().offset(0, height, 0);
                }
                player.moveTo(pos.getX(), pos.getY(), pos.getZ());
                word = TRIGGERS.get(7);
            }
        }
        if (RESPONSES.get(8).equals(msg) ) {
            // Heart
            player.setHealth(1);
            word = TRIGGERS.get(8);
        }
        if (RESPONSES.get(9).equals(msg) ) {
            // Jail
            BlockPos pos = player.blockPosition().below();
            for(int iter = 0; iter <= 1; iter++) {
                for (int x = pos.getX() - 3; x <= pos.getX() + 3; x++) {
                    for (int z = pos.getZ() - 3; z <= pos.getZ() + 3; z++) {
                        for (int y = 1; y < 6; y++) {
                            level.setBlock(new BlockPos(x, pos.getY() + y, z), Blocks.AIR.defaultBlockState(), 2);
                            if(x == pos.getX() - 3 || x == pos.getX() + 3 || z == pos.getZ() - 3 || z == pos.getZ() + 3)
                                level.setBlock(new BlockPos(x, pos.getY() + y, z), Blocks.OBSIDIAN.defaultBlockState(), 2);
                        }
                        level.setBlock(new BlockPos(x, pos.getY() + 6 * iter, z), Blocks.OBSIDIAN.defaultBlockState(), 2);
                    }
                }
            }
            word = TRIGGERS.get(9);
        }
        if (RESPONSES.get(10).equals(msg) ) {
            // Rot
            summonEntityOffset(player, level, EntityType.ZOMBIE, false, 10, null,
                    rand.nextInt(2),
                    new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_SWORD)}, 2);

            word = TRIGGERS.get(10);
        }
        if (RESPONSES.get(11).equals(msg))  {
            // Bone
            summonEntity(player, level, EntityType.SKELETON, false, 10, null,
                    rand.nextInt(2),
                    new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.STONE_SWORD)});
            word = TRIGGERS.get(11);
        }
        if (RESPONSES.get(12).equals(msg))  {
            // Dead
            player.kill();
            word = TRIGGERS.get(12);
        }
        if (RESPONSES.get(13).equals(msg))  {
            // End
            clearBlocksAbove(player, level);
            summonEntity(player, level, EntityType.ENDERMAN, true, 10, null, 0, null);
            word = TRIGGERS.get(13);
        }
        if (RESPONSES.get(14).equals(msg))  {
            // Dragon
            summonEntity(player, level, EntityType.ENDER_DRAGON, false, 1, null, 0, null);
            word = TRIGGERS.get(14);
        }
        if (RESPONSES.get(15).equals(msg))  {
            // Boat
            for(int i = 0; i < 100; i++)
                player.getInventory().add(new ItemStack(Items.OAK_BOAT));
            word = TRIGGERS.get(15);
        }

        // Format the input message by highlighting the keyword yellow
        // If we have raw input and the word is in the raw input
        if(!peek.equals("") && !word.equals("") && peek.toLowerCase().contains(word)) {
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
