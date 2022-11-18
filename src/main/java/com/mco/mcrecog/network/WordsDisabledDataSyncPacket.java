package com.mco.mcrecog.network;

import com.mco.mcrecog.client.ClientWordsDisabledData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WordsDisabledDataSyncPacket {
	private final int disabledTime;

	public WordsDisabledDataSyncPacket(int disabledTime) {
		this.disabledTime = disabledTime;
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(disabledTime);
	}

	public WordsDisabledDataSyncPacket(FriendlyByteBuf buf) {
		this.disabledTime = buf.readInt();
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			ClientWordsDisabledData.setWordsDisabledTime(disabledTime);
		});
		supplier.get().setPacketHandled(true);
		return true;
	}
}
