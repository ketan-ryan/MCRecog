package com.mco.mcrecog;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MCREffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, McRecog.MODID);

    private static boolean isInitialised;

    public static final RegistryObject<GravityEffect> GRAVITY = EFFECTS.register("gravity",
            () -> new GravityEffect(MobEffectCategory.HARMFUL, 2, 2, 2)
    );

    /**
     * Registers the {@link DeferredRegister} instance with the mod event bus.
     * <p>
     * This should be called during mod construction.
     *
     * @param modEventBus The mod event bus
     */
    public static void initialise(final IEventBus modEventBus) {
        if (isInitialised) {
            throw new IllegalStateException("Already initialised");
        }

        EFFECTS.register(modEventBus);

        isInitialised = true;
    }
}
