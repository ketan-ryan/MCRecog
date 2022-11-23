package com.mco.mcrecog.network;

import com.mco.mcrecog.client.ClientDeathData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DeathDataSyncPacket {
	private final int deaths;

	public DeathDataSyncPacket(int deaths) {
		this.deaths = deaths;
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(deaths);
	}

	public DeathDataSyncPacket(FriendlyByteBuf buf) {
		this.deaths = buf.readInt();
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			ClientDeathData.setDeaths(deaths);
		});
		supplier.get().setPacketHandled(true);
		return true;
	}
}
