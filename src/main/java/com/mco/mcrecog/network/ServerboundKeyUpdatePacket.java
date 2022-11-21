package com.mco.mcrecog.network;

import com.mco.mcrecog.entities.corpse.Corpse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ServerboundKeyUpdatePacket {
	public final Integer action;

	public ServerboundKeyUpdatePacket(Integer action) {
		this.action = action;
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(this.action);
	}

	public ServerboundKeyUpdatePacket(FriendlyByteBuf buf) {
		this(buf.readInt());
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		final var success = new AtomicBoolean(false);
		ctx.get().enqueueWork(() -> {
			ServerPlayer sp = ctx.get().getSender();
			if(sp != null && sp.isAlive()) {
				if (this.action == 0) {
					BlockPos blockPos = ctx.get().getSender().blockPosition();
					sp.level.explode(null, DamageSource.badRespawnPointExplosion(),
							null, (double) blockPos.getX() + 0.5D,
							(double) blockPos.getY() + 0.5D,
							(double) blockPos.getZ() + 0.5D, 5.0F, true, Explosion.BlockInteraction.DESTROY);

					Corpse corpse = Corpse.createCorpse(sp);
					sp.level.addFreshEntity(corpse);

					sp.kill();
					success.set(true);
				}
			}

		});
		ctx.get().setPacketHandled(true);
		return success.get();
	}
}
