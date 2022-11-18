package com.mco.mcrecog.capabilities.disabled;

import net.minecraft.nbt.CompoundTag;

public class PlayerWordsDisabled implements IPlayerWordsDisabled {
	private static final int MAX_DISABLED_TIME = 800;
	private int disabledTime;

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
	public void updateTime() {
		if(this.disabledTime > 0) {
			this.disabledTime--;
		}
	}

	@Override
	public void copyFrom(IPlayerWordsDisabled source) {
		this.disabledTime = source.getDisabledTime();
	}

	public void saveNBTData(CompoundTag nbt) {
		nbt.putInt("disabled", disabledTime);
	}

	public void loadNBTData(CompoundTag nbt) {
		this.disabledTime = nbt.getInt("disabled");
	}
}
