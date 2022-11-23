package com.mco.mcrecog.client;

public class ClientWordTimersData {
	private static int playerBeneficence;
	private static int maxBeneficence;
	private static int wordsDisabledTime;

	public static void setPlayerBeneficence(int beneficence) {
		ClientWordTimersData.playerBeneficence = beneficence;
	}

	public static void setMaxBeneficence(int maxBeneficence) {
		ClientWordTimersData.maxBeneficence = maxBeneficence;
	}

	public static int getPlayerBeneficence() {
		return playerBeneficence;
	}

	public static int getMaxBeneficence() {
		return maxBeneficence;
	}

	public static void setWordsDisabledTime(int wordsDisabledTime) {
		ClientWordTimersData.wordsDisabledTime = wordsDisabledTime;
	}

	public static int getWordsDisabledTime() {
		return wordsDisabledTime;
	}
}
