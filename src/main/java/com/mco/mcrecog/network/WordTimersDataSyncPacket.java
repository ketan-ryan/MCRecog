package com.mco.mcrecog.network;

import com.mco.mcrecog.client.ClientWordTimersData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WordTimersDataSyncPacket {
	private final int beneficence;
	private final int maxBeneficence;
	private final int disabledTime;

	public WordTimersDataSyncPacket(int beneficence, int maxBeneficence, int disabledTime) {
		this.beneficence = beneficence;
		this.maxBeneficence = maxBeneficence;
		this.disabledTime = disabledTime;
	}

	public void toBytes(FriendlyByteBuf buf) {
		int [] wordTimers = { beneficence, maxBeneficence, disabledTime };
		buf.writeVarIntArray(wordTimers);
	}

	public WordTimersDataSyncPacket(FriendlyByteBuf buf) {
		int [] wordTimers = buf.readVarIntArray();
		this.beneficence = wordTimers[0];
		this.maxBeneficence = wordTimers[1];
		this.disabledTime = wordTimers[2];
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			ClientWordTimersData.setPlayerBeneficence(beneficence);
			ClientWordTimersData.setMaxBeneficence(maxBeneficence);
			ClientWordTimersData.setWordsDisabledTime(disabledTime);
		});
		supplier.get().setPacketHandled(true);
		return true;
	}
}
