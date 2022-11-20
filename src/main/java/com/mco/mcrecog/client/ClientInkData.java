package com.mco.mcrecog.client;

public class ClientInkData {
	private static int splatTicks;

	public static void setInkSplatTicks(int splatTicks) {
		ClientInkData.splatTicks = splatTicks;
	}

	public static int getInkSplatTicks() {
		return splatTicks;
	}
}
