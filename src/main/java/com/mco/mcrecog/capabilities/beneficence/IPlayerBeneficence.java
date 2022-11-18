package com.mco.mcrecog.capabilities.beneficence;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IPlayerBeneficence {
	int getBeneficence();
	void setMaxBeneficence(int beneficence);
	int getMaxBeneficence();
	void addBeneficence(int beneficence);
	void subBeneficence();
	void copyFrom(IPlayerBeneficence source);
}
