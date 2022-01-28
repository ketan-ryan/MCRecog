package com.mco.mcrecog;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MCRSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, McRecog.MODID);

    public static final RegistryObject<SoundEvent> TONY = SOUNDS.register("tony", () -> new SoundEvent(new ResourceLocation(McRecog.MODID, "tony")));
}
