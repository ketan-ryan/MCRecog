package com.mco.mcrecog.network;

import com.mco.mcrecog.client.ClientGraphicsTimersData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GraphicsTimersDataSyncPacket {
	private final int splatTicks;
	private final int tonyTicks;

	public GraphicsTimersDataSyncPacket(int splatTicks, int tonyTicks) {
		this.splatTicks = splatTicks;
		this.tonyTicks = tonyTicks;
	}

	public void toBytes(FriendlyByteBuf buf) {
		int [] graphicsTimers = { splatTicks, tonyTicks };
		buf.writeVarIntArray(graphicsTimers);
	}

	public GraphicsTimersDataSyncPacket(FriendlyByteBuf buf) {
		int [] graphicsTimers = buf.readVarIntArray();
		this.splatTicks = graphicsTimers[0];
		this.tonyTicks = graphicsTimers[1];
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			ClientGraphicsTimersData.setInkSplatTicks(splatTicks);
			ClientGraphicsTimersData.setTonyTicks(tonyTicks);
		});
		supplier.get().setPacketHandled(true);
		return true;
	}
}
