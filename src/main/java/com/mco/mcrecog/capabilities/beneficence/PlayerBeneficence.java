package com.mco.mcrecog.capabilities.beneficence;

import net.minecraft.nbt.CompoundTag;

public class PlayerBeneficence implements IPlayerBeneficence {
	private int beneficence;
	private int maxBeneficence;

	public int getBeneficence() {
		return beneficence;
	}

	public void setMaxBeneficence(int maxBeneficence) {
		this.maxBeneficence = maxBeneficence;
	}

	public int getMaxBeneficence() {
		return maxBeneficence;
	}

	public void addBeneficence(int beneficence) {
		if(this.beneficence == 0)
			this.beneficence += beneficence;
	}

	public void subBeneficence() {
		if(this.beneficence > 0) {
			this.beneficence--;
		}
	}

	public void copyFrom(IPlayerBeneficence source) {
		this.beneficence = source.getBeneficence();
		this.maxBeneficence = source.getMaxBeneficence();
	}

	public void saveNBTData(CompoundTag nbt) {
		nbt.putInt("beneficence", beneficence);
		nbt.putInt("maxBeneficence", maxBeneficence);
	}

	public void loadNBTData(CompoundTag nbt) {
		this.beneficence = nbt.getInt("beneficence");
		this.maxBeneficence = nbt.getInt("maxBeneficence");
	}
}
