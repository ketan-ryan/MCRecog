package com.mco.mcrecog.capabilities.beneficence;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WordTimersProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
	public static Capability<WordTimers> WORD_TIMERS = CapabilityManager.get(new CapabilityToken<WordTimers>() {});

	private WordTimers wordTimers = null;
	private final LazyOptional<WordTimers> optional = LazyOptional.of(this::createWordTimers);

	private WordTimers createWordTimers() {
		if(this.wordTimers == null) {
			this.wordTimers = new WordTimers();
		}
		return this.wordTimers;
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if(cap == WORD_TIMERS) {
			return optional.cast();
		}
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		createWordTimers().saveNBTData(nbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		createWordTimers().loadNBTData(nbt);
	}
}
