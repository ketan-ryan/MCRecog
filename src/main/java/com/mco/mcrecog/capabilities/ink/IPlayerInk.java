package com.mco.mcrecog.capabilities.ink;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IPlayerInk {
	int getSplatTicks();
	void startSplat();
	void updateSplat();
	void copyFrom(IPlayerInk source);
}
