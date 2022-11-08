package com.mco.mcrecog.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.mco.mcrecog.RecogUtils.*;

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
				ServerLevel level = sp.getLevel();
				switch(this.action) {
					case 0 -> {
						BlockPos blockPos = ctx.get().getSender().blockPosition();
						sp.level.explode(null, DamageSource.badRespawnPointExplosion(),
								null, (double) blockPos.getX() + 0.5D,
								(double) blockPos.getY() + 0.5D,
								(double) blockPos.getZ() + 0.5D, 5.0F, true, Explosion.BlockInteraction.DESTROY);

						sp.kill();
						success.set(true);
					}
					case 1 -> {
						// Food
						sp.getFoodData().setFoodLevel(sp.getFoodData().getFoodLevel() - 20);
						success.set(true);
					}
					case 2 -> {
						// Remove something random
						removeRandomItem(sp);
						success.set(true);
					}
					case 3 -> {
						// Create hole
						BlockPos pos = sp.blockPosition();
						for(int i = 1; i < 22; i++) {
							clearIfNotBedrock(pos.north().offset(0, -i, 0), level);
							clearIfNotBedrock(pos.west().offset(0, -i, 0), level);
							clearIfNotBedrock(pos.south().offset(0, -i, 0), level);
							clearIfNotBedrock(pos.east().offset(0, -i, 0), level);

							clearIfNotBedrock(pos.offset(0, -i, 0), level);

							clearIfNotBedrock(pos.east().north().offset(0, -i, 0),level);
							clearIfNotBedrock(pos.east().south().offset(0, -i, 0),level);
							clearIfNotBedrock(pos.west().north().offset(0, -i, 0),level);
							clearIfNotBedrock(pos.west().south().offset(0, -i, 0),level);
						}
						success.set(true);
					}
					case 4 -> {
						// Mining fatigue
						sp.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 2400, 4));
						success.set(true);
					}
					case 5 -> {
						// Lava
						level.setBlockAndUpdate(sp.blockPosition(), Blocks.LAVA.defaultBlockState());
						success.set(true);
					}
					case 6 -> {
						// Set time to night
						level.setDayTime(20000);
						success.set(true);
					}
					case 7 -> {
						// Drop inventory
						sp.getInventory().dropAll();
						success.set(true);
					}
					case 8 -> {
						// Jump
						int height = 100;
						BlockPos pos = sp.blockPosition().offset(0, height, 0);

						if(!level.dimensionType().hasCeiling()) {
							while (!level.getBlockState(pos).equals(Blocks.AIR.defaultBlockState())) {
								height += 100;
								pos = sp.blockPosition().offset(0, height, 0);
							}
							sp.moveTo(pos.getX(), pos.getY(), pos.getZ());
							success.set(true);
						}
					}
					case 9 -> {
						// Set to half a heart
						sp.setHealth(1);
						success.set(true);
					}
					case 10 -> {
						// Jail
						BlockPos pos = sp.blockPosition().below();
						for(int iter = 0; iter <= 1; iter++) {
							for (int x = pos.getX() - 3; x <= pos.getX() + 3; x++) {
								for (int z = pos.getZ() - 3; z <= pos.getZ() + 3; z++) {
									for (int y = 1; y < 6; y++) {
										level.setBlock(new BlockPos(x, pos.getY() + y, z), Blocks.AIR.defaultBlockState(), 2);
										if(x == pos.getX() - 3 || x == pos.getX() + 3 || z == pos.getZ() - 3 || z == pos.getZ() + 3)
											level.setBlock(new BlockPos(x, pos.getY() + y, z), Blocks.OBSIDIAN.defaultBlockState(), 2);
									}
									level.setBlock(new BlockPos(x, pos.getY() + 6 * iter, z), Blocks.OBSIDIAN.defaultBlockState(), 2);
								}
							}
						}
						success.set(true);
					}
					case 11 -> {
						// Rot
						summonEntityOffset(sp, level, EntityType.ZOMBIE, false, 10, null,
								rand.nextInt(2),
								new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_SWORD)}, 2);
						success.set(true);
					}
					case 12 -> {
						// Bone
						summonEntity(sp, level, EntityType.SKELETON, false, 10, null,
								rand.nextInt(2),
								new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.STONE_SWORD)});
						success.set(true);
					}
					case 13 -> {
						// Dead
						sp.kill();
						success.set(true);
					}
					case 14 -> {
						// End
						clearBlocksAbove(sp, level);
						summonEntity(sp, level, EntityType.ENDERMAN, true, 10, null, 0, null);
						success.set(true);
					}
					case 15 -> {
						// Dragon
						summonEntity(sp, level, EntityType.ENDER_DRAGON, false, 1, null, 0, null);
						success.set(true);
					}
					case 16 -> {
						// Boat
						for(int i = 0; i < 100; i++)
							sp.getInventory().add(new ItemStack(Items.OAK_BOAT));
						success.set(true);
					}
					default -> {}
				}
			}

		});
		ctx.get().setPacketHandled(true);
		return success.get();
	}
}
