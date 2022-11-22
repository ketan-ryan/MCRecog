package com.mco.mcrecog.capabilities.timers;

import net.minecraft.nbt.CompoundTag;

import static com.mco.mcrecog.RecogUtils.TONY_TICKS;

public class GraphicsTimers implements IGraphicsTimers {
	private static final int SPLAT_TICKS = 400;
	private int splatTicks;

	private int tonyTicks = 90;

	@Override
	public int getTonyTicks() {
		return tonyTicks;
	}

	@Override
	public void startTony() {
		if(this.tonyTicks == TONY_TICKS) {
			this.tonyTicks = 0;
		}
	}

	@Override
	public void updateTony() {
		if(this.tonyTicks < TONY_TICKS) {
			this.tonyTicks++;
		}
	}

	@Override
	public int getSplatTicks() {
		return splatTicks;
	}

	@Override
	public void startSplat() {
		if(this.splatTicks == 0) {
			this.splatTicks = SPLAT_TICKS;
		}
	}

	@Override
	public void updateSplat() {
		if(this.splatTicks > 0) {
			this.splatTicks--;
		}
	}

	@Override
	public void copyFrom(IGraphicsTimers source) {
		this.splatTicks = source.getSplatTicks();
		this.tonyTicks = source.getTonyTicks();
	}

	public void saveNBTData(CompoundTag nbt) {
		nbt.putInt("splat", splatTicks);
		nbt.putInt("tony", tonyTicks);
	}

	public void loadNBTData(CompoundTag nbt) {
		this.splatTicks = nbt.getInt("splat");
		this.tonyTicks = nbt.getInt("tony");
	}
}
