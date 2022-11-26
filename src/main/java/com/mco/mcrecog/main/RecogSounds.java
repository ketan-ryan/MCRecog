package com.mco.mcrecog.main;

import com.mco.mcrecog.MCRecog;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

public class RecogSounds extends SoundDefinitionsProvider {
	/**
	 * Creates a new instance of this data provider.
	 *
	 * @param generator The data generator instance provided by the event you are initializing this provider in.
	 * @param modId     The mod ID of the current mod.
	 * @param helper    The existing file helper provided by the event you are initializing this provider in.
	 */
	public RecogSounds(DataGenerator generator, String modId, ExistingFileHelper helper) {
		super(generator, modId, helper);
	}

	public static final SoundEvent TONY = new SoundEvent(new ResourceLocation(MCRecog.MODID, "tony"));

	@Override
	public void registerSounds() {
		this.add(TONY,
				definition()
						.subtitle("sound.mcrecog.tony")
						.with(
								sound(new ResourceLocation(MCRecog.MODID, "tony"))
						));
	}
}
