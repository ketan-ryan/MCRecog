package com.mco.mcrecog.capabilities;

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

public class PlayerBeneficenceProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
	public static Capability<PlayerBeneficence> PLAYER_BENEFICENCE = CapabilityManager.get(new CapabilityToken<PlayerBeneficence>() {});

	private PlayerBeneficence beneficence = null;
	private final LazyOptional<PlayerBeneficence> optional = LazyOptional.of(this::createPlayerBeneficence);

	private PlayerBeneficence createPlayerBeneficence() {
		if(this.beneficence == null) {
			this.beneficence = new PlayerBeneficence();
		}
		return this.beneficence;
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if(cap == PLAYER_BENEFICENCE) {
			return optional.cast();
		}
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		createPlayerBeneficence().saveNBTData(nbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		createPlayerBeneficence().loadNBTData(nbt);
	}
}
