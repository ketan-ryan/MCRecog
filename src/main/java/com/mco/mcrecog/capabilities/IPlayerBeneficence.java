package com.mco.mcrecog.capabilities;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IPlayerBeneficence {
	int getBeneficence();
	void setMaxBeneficence(int beneficence);
	void addBeneficence(int beneficence);
	void subBeneficence();
	void copyFrom(IPlayerBeneficence source);
}
