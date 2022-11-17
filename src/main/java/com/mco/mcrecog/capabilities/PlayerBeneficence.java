package com.mco.mcrecog.capabilities;

import net.minecraft.nbt.CompoundTag;

public class PlayerBeneficence implements IPlayerBeneficence {
	private int beneficence;
	private final int MIN_BENEFICENCE = 0;
	private int MAX_BENEFICENCE;

	public int getBeneficence() {
		return beneficence;
	}

	public void setMaxBeneficence(int maxBeneficence) {
		this.MAX_BENEFICENCE = maxBeneficence;
	}

	public int getMaxBeneficence() {
		return MAX_BENEFICENCE;
	}

	public void addBeneficence(int beneficence) {
		if(this.beneficence == 0)
			this.beneficence += beneficence;
	}

	public void subBeneficence() {
		this.beneficence = Math.max(MIN_BENEFICENCE, --this.beneficence);
	}

	public void copyFrom(IPlayerBeneficence source) {
		this.beneficence = source.getBeneficence();
	}

	public void saveNBTData(CompoundTag nbt) {
		nbt.putInt("beneficence", beneficence);
	}

	public void loadNBTData(CompoundTag nbt) {
		this.beneficence = nbt.getInt("beneficence");
	}
}
