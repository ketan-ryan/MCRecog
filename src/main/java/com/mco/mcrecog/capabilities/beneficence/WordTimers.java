package com.mco.mcrecog.capabilities.beneficence;

import net.minecraft.nbt.CompoundTag;

public class WordTimers implements IWordTimers {
	private int beneficence;
	private int maxBeneficence;

	private static final int MAX_DISABLED_TIME = 800;
	private int disabledTime;

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

	public void updateBeneficence() {
		if(this.beneficence > 0) {
			this.beneficence--;
		}
	}

	@Override
	public int getDisabledTime() {
		return disabledTime;
	}

	@Override
	public void setDisabled() {
		if(this.disabledTime == 0) {
			this.disabledTime = MAX_DISABLED_TIME;
		}
	}

	@Override
	public void updateDisabledTime() {
		if(this.disabledTime > 0) {
			this.disabledTime--;
		}
	}

	public void copyFrom(IWordTimers source) {
		this.beneficence = source.getBeneficence();
		this.maxBeneficence = source.getMaxBeneficence();
		this.disabledTime = source.getDisabledTime();
	}

	public void saveNBTData(CompoundTag nbt) {
		nbt.putInt("beneficence", beneficence);
		nbt.putInt("maxBeneficence", maxBeneficence);
		nbt.putInt("disabled", disabledTime);
	}

	public void loadNBTData(CompoundTag nbt) {
		this.beneficence = nbt.getInt("beneficence");
		this.maxBeneficence = nbt.getInt("maxBeneficence");
		this.disabledTime = nbt.getInt("disabled");
	}
}
