package com.mco.mcrecog;

import com.mco.mcrecog.network.RecogPacketHandler;
import com.mco.mcrecog.network.ServerboundKeyUpdatePacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MCRecog.MODID)
public class MCRecog
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "mcrecog";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    /*// Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));
*/
    public MCRecog()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
//        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
//        ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        RecogPacketHandler.init();
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

        switch (event.getKey()) {
            case GLFW.GLFW_KEY_P -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(0));
            case GLFW.GLFW_KEY_X -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(1));
            case GLFW.GLFW_KEY_R -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(2));
            case GLFW.GLFW_KEY_B -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(3));
            case GLFW.GLFW_KEY_Y -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(4));
            case GLFW.GLFW_KEY_U -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(5));
            case GLFW.GLFW_KEY_I -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(6));
            case GLFW.GLFW_KEY_O -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(7));
            case GLFW.GLFW_KEY_V -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(8));
            case GLFW.GLFW_KEY_COMMA -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(9));
            case GLFW.GLFW_KEY_K -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(10));
            case GLFW.GLFW_KEY_J -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(11));
            case GLFW.GLFW_KEY_H -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(12));
            case GLFW.GLFW_KEY_G -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(13));
            case GLFW.GLFW_KEY_Z -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(14));
            case GLFW.GLFW_KEY_M -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(15));
            case GLFW.GLFW_KEY_N -> RecogPacketHandler.sendToServer(new ServerboundKeyUpdatePacket(16));

            default -> {}
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
