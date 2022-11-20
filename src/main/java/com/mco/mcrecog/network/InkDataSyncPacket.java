package com.mco.mcrecog.network;

import com.mco.mcrecog.client.ClientInkData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InkDataSyncPacket {
	private final int splatTicks;

	public InkDataSyncPacket(int splatTicks) {
		this.splatTicks = splatTicks;
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(splatTicks);
	}

	public InkDataSyncPacket(FriendlyByteBuf buf) {
		this.splatTicks = buf.readInt();
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			ClientInkData.setInkSplatTicks(splatTicks);
		});
		supplier.get().setPacketHandled(true);
		return true;
	}
}
