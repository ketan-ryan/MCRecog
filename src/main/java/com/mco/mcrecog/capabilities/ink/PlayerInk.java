package com.mco.mcrecog.capabilities.ink;

import net.minecraft.nbt.CompoundTag;

public class PlayerInk implements IPlayerInk {
	private static final int SPLAT_TICKS = 400;
	private int ticks;

	@Override
	public int getSplatTicks() {
		return ticks;
	}

	@Override
	public void startSplat() {
		if(this.ticks == 0) {
			this.ticks = SPLAT_TICKS;
		}
	}

	@Override
	public void updateSplat() {
		if(this.ticks > 0) {
			this.ticks--;
		}
	}

	@Override
	public void copyFrom(IPlayerInk source) {
		this.ticks = source.getSplatTicks();
	}

	public void saveNBTData(CompoundTag nbt) {
		nbt.putInt("splat", ticks);
	}

	public void loadNBTData(CompoundTag nbt) {
		this.ticks = nbt.getInt("splat");
	}
}
