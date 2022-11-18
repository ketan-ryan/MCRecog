package com.mco.mcrecog.capabilities.disabled;

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

public class PlayerWordsDisabledProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
	public static Capability<PlayerWordsDisabled> PLAYER_WORDS_DISABLED = CapabilityManager.get(new CapabilityToken<>() {});

	private PlayerWordsDisabled wordsDisabled = null;
	private final LazyOptional<PlayerWordsDisabled> optional = LazyOptional.of(this::createPlayerWordsDisabled);

	private PlayerWordsDisabled createPlayerWordsDisabled() {
		if(this.wordsDisabled == null) {
			this.wordsDisabled = new PlayerWordsDisabled();
		}
		return this.wordsDisabled;
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if(cap == PLAYER_WORDS_DISABLED) {
			return optional.cast();
		}
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		createPlayerWordsDisabled().saveNBTData(nbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		createPlayerWordsDisabled().loadNBTData(nbt);
	}
}

