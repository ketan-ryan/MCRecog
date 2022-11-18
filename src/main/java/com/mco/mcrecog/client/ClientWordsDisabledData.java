package com.mco.mcrecog.client;

public class ClientWordsDisabledData {
	private static int wordsDisabledTime;

	public static void setWordsDisabledTime(int wordsDisabledTime) {
		ClientWordsDisabledData.wordsDisabledTime = wordsDisabledTime;
	}

	public static int getWordsDisabledTime() {
		return wordsDisabledTime;
	}
}
