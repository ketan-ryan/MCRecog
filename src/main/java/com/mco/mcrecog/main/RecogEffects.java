package com.mco.mcrecog.main;

import com.mco.mcrecog.MCRecog;
import com.mco.mcrecog.effects.GravityEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecogEffects {
	private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MCRecog.MODID);
	private static boolean isInitialized;

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
		if (isInitialized) {
			throw new IllegalStateException("Already initialised");
		}

		EFFECTS.register(modEventBus);
		isInitialized = true;
	}
}
