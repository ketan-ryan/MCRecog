package com.mco.mcrecog;

import com.mco.mcrecog.capabilities.beneficence.PlayerBeneficenceProvider;
import com.mco.mcrecog.capabilities.disabled.PlayerWordsDisabledProvider;
import com.mco.mcrecog.capabilities.ink.PlayerInkProvider;
import com.mco.mcrecog.client.ClientWordsDisabledData;
import com.mco.mcrecog.client.RecogGui;
import com.mco.mcrecog.network.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.mco.mcrecog.RecogUtils.RESPONSES;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MCRecog.MODID)
public class MCRecog
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "mcrecog";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // The web socket
    private ServerSocket server;
    // The blocking (thread-safe) queue to put our input onto in order to communicate between the socket thread and the main thread
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    // The blocking queue for client operations
    private final BlockingQueue<String> clientQueue = new LinkedBlockingQueue<>();

    public MCRecog()
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::connectToSocket);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        RecogEffects.initialise(modEventBus);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, RecogConfig.GENERAL_SPEC);
    }

    private void connectToSocket() {
        // Connect to the server
        try {
            server = new ServerSocket(7777);
        } catch (IOException e){
            LOGGER.error(e.getMessage());
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
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        RecogPacketHandler.init();
    }

    @SubscribeEvent
    public void onAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(PlayerBeneficenceProvider.PLAYER_BENEFICENCE).isPresent()) {
                event.addCapability(new ResourceLocation(MODID, "beneficence"), new PlayerBeneficenceProvider());
            }
            if(!event.getObject().getCapability(PlayerWordsDisabledProvider.PLAYER_WORDS_DISABLED).isPresent()) {
                event.addCapability(new ResourceLocation(MODID, "disabled"), new PlayerWordsDisabledProvider());
            }
            if(!event.getObject().getCapability(PlayerInkProvider.PLAYER_INK_SPLAT).isPresent()) {
                event.addCapability(new ResourceLocation(MODID, "splat"), new PlayerInkProvider());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone event) {
        if(event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerBeneficenceProvider.PLAYER_BENEFICENCE).ifPresent(oldStore -> {
                event.getOriginal().getCapability(PlayerBeneficenceProvider.PLAYER_BENEFICENCE).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
            event.getOriginal().getCapability(PlayerWordsDisabledProvider.PLAYER_WORDS_DISABLED).ifPresent(oldStore -> {
                event.getOriginal().getCapability(PlayerWordsDisabledProvider.PLAYER_WORDS_DISABLED).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
            event.getOriginal().getCapability(PlayerInkProvider.PLAYER_INK_SPLAT).ifPresent(oldStore -> {
                event.getOriginal().getCapability(PlayerInkProvider.PLAYER_INK_SPLAT).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    @SubscribeEvent
    public void onPlayerJoinWorldEvent(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(PlayerBeneficenceProvider.PLAYER_BENEFICENCE).ifPresent(playerBeneficence -> {
                    RecogPacketHandler.sendToClient(new BeneficenceDataSyncPacket(playerBeneficence.getBeneficence(), playerBeneficence.getMaxBeneficence()), player);
                });
                player.getCapability(PlayerWordsDisabledProvider.PLAYER_WORDS_DISABLED).ifPresent(wordsDisabled -> {
                    RecogPacketHandler.sendToClient(new WordsDisabledDataSyncPacket(wordsDisabled.getDisabledTime()), player);
                });
                player.getCapability(PlayerInkProvider.PLAYER_INK_SPLAT).ifPresent(inkSplat -> {
                    RecogPacketHandler.sendToClient(new InkDataSyncPacket(inkSplat.getSplatTicks()), player);
                });
            }
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onKeyEvent(InputEvent.Key event) {
        if(event.getAction() != InputConstants.PRESS) return;
        if(event.getKey() == GLFW.GLFW_KEY_B) {
            RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(37));
        }
    }

    @SubscribeEvent
    public void onServerTickEvent(TickEvent.PlayerTickEvent event) {
        if(event.side == LogicalSide.CLIENT) return;
        ServerPlayer player = (ServerPlayer) event.player;

        event.player.getCapability(PlayerBeneficenceProvider.PLAYER_BENEFICENCE).ifPresent(playerBeneficence -> {
            playerBeneficence.subBeneficence();
            RecogPacketHandler.sendToClient(new BeneficenceDataSyncPacket(playerBeneficence.getBeneficence(), playerBeneficence.getMaxBeneficence()), player);
        });

        event.player.getCapability(PlayerWordsDisabledProvider.PLAYER_WORDS_DISABLED).ifPresent(wordsDisabled -> {
            wordsDisabled.updateTime();
            RecogPacketHandler.sendToClient(new WordsDisabledDataSyncPacket(wordsDisabled.getDisabledTime()), player);
        });

        event.player.getCapability(PlayerInkProvider.PLAYER_INK_SPLAT).ifPresent(inkSplat -> {
            inkSplat.updateSplat();
            RecogPacketHandler.sendToClient(new InkDataSyncPacket(inkSplat.getSplatTicks()), player);
        });

        RecogPacketHandler.sendToClient(new DeathDataSyncPacket(player.getStats().getValue(Stats.CUSTOM, Stats.DEATHS)), player);
    }

    @SubscribeEvent
    public void onTickEvent(TickEvent.PlayerTickEvent event) {
        if(event.side == LogicalSide.SERVER) return;

        if(ClientWordsDisabledData.getWordsDisabledTime() > 0) return;

        String msg;
        while ((msg = queue.poll()) != null) {
            event.player.sendSystemMessage(Component.literal(msg));

            for (int i = 0; i < 42; i++) {
                if (RESPONSES.get(i).equals(msg)) {
                    RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(i));

                    SoundEvent soundEvent = null;
                    float volume = 1.0F;
                    if (i == 15) {
                        soundEvent = SoundEvents.ENDER_DRAGON_GROWL;
                    }
                    else if (i == 29) {
                        soundEvent = SoundEvents.CHORUS_FRUIT_TELEPORT;
                    }
                    else if (i == 36) {
                        soundEvent = SoundEvents.SLIME_SQUISH;
                        volume = 10.0F;
                    }
                    if(soundEvent != null) {
                        event.player.level.playLocalSound(event.player.position().x, event.player.position().y, event.player.position().z, soundEvent, SoundSource.MASTER, volume, 1.0F, false);
                    }
                }
            }
            /*// Food
            if(RESPONSES.get(0).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(1));
            }
            // Lose something random
            if(RESPONSES.get(1).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(2));
            }
            // Hole
            if(RESPONSES.get(2).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(3));
            }
            // Mining Fatigue
            if(RESPONSES.get(3).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(4));
            }
            // Lava
            if(RESPONSES.get(4).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(5));
            }
            // Nighttime
            if(RESPONSES.get(5).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(6));
            }
            // Drop inventory
            if(RESPONSES.get(6).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(7));
            }
            // Launched in air (jump)
            if(RESPONSES.get(7).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(8));
            }
            // Set to half a heart
            if(RESPONSES.get(8).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(9));
            }
            // Surround in obsidian
            if(RESPONSES.get(9).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(10));
            }
            // Spawn zombies
            if(RESPONSES.get(10).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(11));
            }
            // Spawn skeletons
            if(RESPONSES.get(11).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(12));
            }
            // Instant death
            if(RESPONSES.get(12).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(13));
            }
            // Spawn endermen
            if(RESPONSES.get(13).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(14));
            }
            // Spawn dragon
            if(RESPONSES.get(14).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(15));
            }
            // Boats
            if(RESPONSES.get(15).equals(msg)) {
                RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(16));
            }*/
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("bars", RecogGui.HUD_BARS);
            event.registerAboveAll("deaths", RecogGui.HUD_DEATHS);
            event.registerAboveAll("ink", RecogGui.HUD_INK);
        }
    }
}
