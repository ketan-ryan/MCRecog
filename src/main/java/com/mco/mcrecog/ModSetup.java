package com.mco.mcrecog;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = McRecog.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSetup {
	public static void init(FMLCommonSetupEvent event) {
		MCPacketHandler.init();
	}
}
