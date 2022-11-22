package com.mco.mcrecog.client;

public class ClientGraphicsTimersData {
	private static int splatTicks;
	private static int tonyTicks;

	public static void setInkSplatTicks(int splatTicks) {
		ClientGraphicsTimersData.splatTicks = splatTicks;
	}

	public static int getInkSplatTicks() {
		return splatTicks;
	}

	public static void setTonyTicks(int tonyTicks) {
		ClientGraphicsTimersData.tonyTicks = tonyTicks;
	}

	public static int getTonyTicks() {
		return tonyTicks;
	}
}
