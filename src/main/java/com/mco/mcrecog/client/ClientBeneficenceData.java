package com.mco.mcrecog.client;

public class ClientBeneficenceData {
	private static int playerBeneficence;
	private static int maxBeneficence;

	public static void setPlayerBeneficence(int beneficence) {
		ClientBeneficenceData.playerBeneficence = beneficence;
	}

	public static void setMaxBeneficence(int maxBeneficence) {
		ClientBeneficenceData.maxBeneficence = maxBeneficence;
	}

	public static int getPlayerBeneficence() {
		return playerBeneficence;
	}

	public static int getMaxBeneficence() {
		return maxBeneficence;
	}
}
