package com.mco.mcrecog.capabilities.disabled;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IPlayerWordsDisabled {
	int getDisabledTime();
	void setDisabled();
	void updateTime();
	void copyFrom(IPlayerWordsDisabled source);
}
