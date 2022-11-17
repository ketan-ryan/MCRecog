package com.mco.mcrecog.network;

import com.mco.mcrecog.client.ClientBeneficenceData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BeneficenceDataSyncPacket {
	private final int beneficence;
	private final int maxBeneficence;

	public BeneficenceDataSyncPacket(int beneficence, int maxBeneficence) {
		this.beneficence = beneficence;
		this.maxBeneficence = maxBeneficence;
	}

	public void toBytes(FriendlyByteBuf buf) {
		int [] benInfo = {beneficence, maxBeneficence};
		buf.writeVarIntArray(benInfo);
	}

	public BeneficenceDataSyncPacket(FriendlyByteBuf buf) {
		int [] benInfo = buf.readVarIntArray();
		this.beneficence = benInfo[0];
		this.maxBeneficence = benInfo[1];
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			ClientBeneficenceData.setPlayerBeneficence(beneficence);
			ClientBeneficenceData.setMaxBeneficence(maxBeneficence);
		});
		supplier.get().setPacketHandled(true);
		return true;
	}
}
