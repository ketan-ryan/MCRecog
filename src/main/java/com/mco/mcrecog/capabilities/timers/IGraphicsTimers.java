package com.mco.mcrecog.capabilities.timers;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IGraphicsTimers {
	int getSplatTicks();
	void startSplat();
	void updateSplat();

	int getTonyTicks();
	void startTony();
	void updateTony();

	void copyFrom(IGraphicsTimers source);
}
