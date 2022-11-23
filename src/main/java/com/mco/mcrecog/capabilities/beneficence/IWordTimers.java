package com.mco.mcrecog.capabilities.beneficence;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IWordTimers {
	int getBeneficence();
	void addBeneficence(int beneficence);
	void updateBeneficence();

	void setMaxBeneficence(int beneficence);
	int getMaxBeneficence();

	int getDisabledTime();
	void setDisabled();
	void updateDisabledTime();

	void copyFrom(IWordTimers source);
}
