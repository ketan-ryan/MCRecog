package com.mco.mcrecog.entities.corpse;

import com.mco.mcrecog.RecogEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class Corpse extends Entity {

	private static final EntityDataAccessor<Optional<UUID>> ID = SynchedEntityData.defineId(Corpse.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(Corpse.class, EntityDataSerializers.STRING);

	public Corpse(EntityType<? extends Entity> type, Level level) {
		super(type, level);
	}

	public static Corpse createCorpse(Player player) {
		Corpse corpse = new Corpse(RecogEntities.CORPSE.get(), player.level);
		corpse.setCorpseUUID(player.getUUID());
		corpse.setCorpseName(player.getName().getString());
		corpse.setPos(player.position());
		return corpse;
	}

	public void setCorpseUUID(UUID uuid) {
		if(uuid == null) {
			entityData.set(ID, Optional.empty());
		} else {
			entityData.set(ID, Optional.of(uuid));
		}
	}

	public void setCorpseName(String name) {
		entityData.set(NAME, name);
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(ID, Optional.empty());
		entityData.define(NAME, "");
	}

	@Override
	public void tick() {
		super.tick();
		if(this.level.getBlockState(new BlockPos(position())).is(Blocks.AIR)) {
			this.setDeltaMovement(getDeltaMovement().x, -0.25D, getDeltaMovement().z);
			this.move(MoverType.SELF, getDeltaMovement());
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		UUID playerId = tag.getUUID("PlayerID");
		String name = tag.getString("Name");

		setCorpseUUID(playerId);
		setCorpseName(name);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putUUID("PlayerID", getCorpseUUID().orElse(new UUID(0L, 0L)));
		tag.putString("Name", getCorpseName());
	}

	public Optional<UUID> getCorpseUUID() {
		return entityData.get(ID);
	}

	public String getCorpseName() {
		return entityData.get(NAME);
	}

	@Override
	public @NotNull Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}
}
