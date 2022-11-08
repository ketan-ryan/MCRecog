package com.mco.mcrecog.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
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
			if(ctx.get().getSender() != null) {
				if(this.action == 1) {
					BlockPos blockPos = ctx.get().getSender().blockPosition();
					ctx.get().getSender().level.explode((Entity) null, DamageSource.badRespawnPointExplosion(),
							(ExplosionDamageCalculator) null, (double) blockPos.getX() + 0.5D,
							(double) blockPos.getY() + 0.5D,
							(double) blockPos.getZ() + 0.5D, 5.0F, true, Explosion.BlockInteraction.DESTROY);

					ctx.get().getSender().kill();
					success.set(true);
				}
			}

		});
		ctx.get().setPacketHandled(true);
		return success.get();
	}
}
