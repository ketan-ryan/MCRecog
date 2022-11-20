package com.mco.mcrecog.capabilities.ink;

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

public class PlayerInkProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
	public static Capability<PlayerInk> PLAYER_INK_SPLAT = CapabilityManager.get(new CapabilityToken<>() {});

	private PlayerInk inkSplat = null;
	private final LazyOptional<PlayerInk> optional = LazyOptional.of(this::createPlayerInk);

	private PlayerInk createPlayerInk() {
		if(this.inkSplat == null) {
			this.inkSplat = new PlayerInk();
		}
		return this.inkSplat;
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if(cap == PLAYER_INK_SPLAT) {
			return optional.cast();
		}
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		createPlayerInk().saveNBTData(nbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		createPlayerInk().loadNBTData(nbt);
	}
}
