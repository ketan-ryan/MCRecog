package com.mco.mcrecog.network;

import com.mco.mcrecog.main.RecogConfig;
import com.mco.mcrecog.main.RecogEffects;
import com.mco.mcrecog.main.RecogStats;
import com.mco.mcrecog.capabilities.beneficence.WordTimersProvider;
import com.mco.mcrecog.capabilities.timers.GraphicsTimersProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.mco.mcrecog.main.RecogUtils.*;

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
			Team team = sp.getTeam();
			ArrayList<ServerPlayer> players = new ArrayList<>();
			if(team != null && RecogConfig.affectTeam.get()) {
				for(String s : team.getPlayers()) {
					players.add(sp.getServer().getPlayerList().getPlayerByName(s));
				}
			} else {
				players.add(sp);
			}

			for (ServerPlayer player : players) {
					if (player != null && player.isAlive()) {

						ServerLevel level = player.getLevel();
						switch (this.action) {
							case 0 -> {
								BlockPos blockPos = ctx.get().getSender().blockPosition();
								player.level.explode(null, DamageSource.badRespawnPointExplosion(),
										null, (double) blockPos.getX() + 0.5D,
										(double) blockPos.getY() + 0.5D,
										(double) blockPos.getZ() + 0.5D, 5.0F, true, Explosion.BlockInteraction.DESTROY);

								player.kill();
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_CROW.get());
								}
								success.set(true);
							}
							case 1 -> {
								// Food
								player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 20);
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_PIG.get());
								}
								success.set(true);
							}
							case 2 -> {
								// Remove something random
								int chances = rand.nextInt(41);
								if (chances < 36) {
									// Remove random item from inventory
									removeRandomItem(player);
								} else if (chances == 37) {
									// Clear offhand
									if (!player.getInventory().offhand.get(0).isEmpty())
										player.getInventory().offhand.clear();
								} else {
									// Remove random armor piece
									boolean empty = true;
									for (ItemStack stack : player.getInventory().armor) {
										if (!stack.isEmpty())
											empty = false;
									}
									if (empty) {
										success.set(true);
										break;
									}

									int slot = rand.nextInt(4);
									while (player.getInventory().armor.get(slot).equals(ItemStack.EMPTY))
										slot = rand.nextInt(4);

									player.getInventory().armor.get(slot).shrink(1);
								}
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_SUB.get());
								}
								success.set(true);
							}
							case 3 -> {
								// Create hole
								BlockPos pos = player.blockPosition();
								for (int i = 1; i < 22; i++) {
									clearIfNotBedrock(pos.north().offset(0, -i, 0), level);
									clearIfNotBedrock(pos.west().offset(0, -i, 0), level);
									clearIfNotBedrock(pos.south().offset(0, -i, 0), level);
									clearIfNotBedrock(pos.east().offset(0, -i, 0), level);

									clearIfNotBedrock(pos.offset(0, -i, 0), level);

									clearIfNotBedrock(pos.east().north().offset(0, -i, 0), level);
									clearIfNotBedrock(pos.east().south().offset(0, -i, 0), level);
									clearIfNotBedrock(pos.west().north().offset(0, -i, 0), level);
									clearIfNotBedrock(pos.west().south().offset(0, -i, 0), level);
								}
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_FOLLOW.get());
								}
								success.set(true);
							}
							case 4 -> {
								// Mining fatigue
								player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 2400, 4));
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_CAVE.get());
								}
								success.set(true);
							}
							case 5 -> {
								// Lava
								level.setBlockAndUpdate(player.blockPosition(), Blocks.LAVA.defaultBlockState());
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_YIKE.get());
								}
								success.set(true);
							}
							case 6 -> {
								// Set time to night
								level.setDayTime(20000);
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_DAY.get());
								}
								success.set(true);
							}
							case 7 -> {
								// Drop inventory
								player.getInventory().dropAll();
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_TROLL.get());
								}
								success.set(true);
							}
							case 8 -> {
								// Jump
								int height = 100;
								BlockPos pos = player.blockPosition().offset(0, height, 0);

								if (!level.dimensionType().hasCeiling()) {
									while (!level.getBlockState(pos).equals(Blocks.AIR.defaultBlockState())) {
										height += 100;
										pos = player.blockPosition().offset(0, height, 0);
									}
									player.moveTo(pos.getX(), pos.getY(), pos.getZ());
									if(player.equals(sp)) {
										player.awardStat(RecogStats.STAT_HIGH.get());
									}
									success.set(true);
								}
							}
							case 9 -> {
								// Set to half a heart
								player.setHealth(1);
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_DIAMOND.get());
								}
								success.set(true);
							}
							case 10 -> {
								// Jail
								BlockPos pos = player.blockPosition().below();
								for (int iter = 0; iter <= 1; iter++) {
									for (int x = pos.getX() - 3; x <= pos.getX() + 3; x++) {
										for (int z = pos.getZ() - 3; z <= pos.getZ() + 3; z++) {
											for (int y = 1; y < 6; y++) {
												level.setBlock(new BlockPos(x, pos.getY() + y, z), Blocks.AIR.defaultBlockState(), 2);
												if (x == pos.getX() - 3 || x == pos.getX() + 3 || z == pos.getZ() - 3 || z == pos.getZ() + 3)
													level.setBlock(new BlockPos(x, pos.getY() + y, z), Blocks.OBSIDIAN.defaultBlockState(), 2);
											}
											level.setBlock(new BlockPos(x, pos.getY() + 6 * iter, z), Blocks.OBSIDIAN.defaultBlockState(), 2);
										}
									}
								}
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_CRAFT.get());
								}
								success.set(true);
							}
							case 11 -> {
								// Rot
								summonEntityOffset(player, level, EntityType.ZOMBIE, false, 10, null,
										rand.nextInt(2),
										new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_SWORD)}, 2);
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_ROT.get());
								}
								success.set(true);
							}
							case 12 -> {
								// Bone
								summonEntity(player, level, EntityType.SKELETON, false, 10, null,
										rand.nextInt(2),
										new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.STONE_SWORD)});
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_BONE.get());
								}
								success.set(true);
							}
							case 13 -> {
								// Dead
								player.kill();
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_DREAM.get());
								}
								success.set(true);
							}
							case 14 -> {
								// End
								clearBlocksAbove(player, level);
								summonEntity(player, level, EntityType.ENDERMAN, true, 10, null, 0, null);
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_END.get());
								}
								success.set(true);
							}
							case 15 -> {
								// Dragon
								summonEntity(player, level, EntityType.ENDERMITE, true, 10, null, 0, null);
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_DRAGON.get());
								}
								success.set(true);
							}
							case 16 -> {
								// Boat
								for (int i = 0; i < 100; i++)
									player.getInventory().add(new ItemStack(Items.OAK_BOAT));
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_BOAT.get());
								}
								success.set(true);
							}
							case 17 -> {
								// No shot
								int slot = player.getInventory().findSlotMatchingItem(new ItemStack(Items.ARROW));
								if (slot != -1) {
									player.getInventory().removeItem(slot, 10);
								}
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_NO_SHOT.get());
								}
								success.set(true);
							}
							case 18 -> {
								// Bear
								summonEntityOffset(player, level, EntityType.POLAR_BEAR, true, 7, null, 0, null, 4);
								if (RecogConfig.waterWhenSpawning.get() && player.isInWater())
									player.addEffect(new MobEffectInstance(RecogEffects.GRAVITY.get(), 1200, 0));
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_BEAR.get());
								}
								success.set(true);
							}
							case 19 -> {
								// Axolotl
								player.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 1));
								player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 1));
								player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 1));
								summonEntity(player, level, EntityType.TROPICAL_FISH, false, 15, null, 0, null);

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_AXOLOTL.get());
								}
								success.set(true);
							}
							case 20 -> {
								// Creep
								summonEntityOffset(player, level, EntityType.CREEPER, false, 7, null, 0, null, 2);
								if (RecogConfig.waterWhenSpawning.get() && player.isInWater())
									player.addEffect(new MobEffectInstance(RecogEffects.GRAVITY.get(), 1200, 0));

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_CREEP.get());
								}
								success.set(true);
							}
							case 21 -> {
								// Rod
								summonEntity(player, level, EntityType.BLAZE, false, 7, null, 0, null);
								if (RecogConfig.waterWhenSpawning.get() && player.isInWater())
									player.addEffect(new MobEffectInstance(RecogEffects.GRAVITY.get(), 1200, 0));

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_ROD.get());
								}
								success.set(true);
							}
							case 22 -> {
								// Nether
								clearBlocksAbove(player, level);
								summonEntity(player, level, EntityType.WITHER_SKELETON, false, 7, MobEffects.MOVEMENT_SPEED, 2,
										new ItemStack[]{new ItemStack(Items.GOLDEN_SWORD)});

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_NETHER.get());
								}
								success.set(true);
							}
							case 23 -> {
								// Bed
								summonEntity(player, level, EntityType.PHANTOM, false, 7, MobEffects.DAMAGE_RESISTANCE,
										2, null);

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_BED.get());
								}
								success.set(true);
							}
							case 24 -> {
								// Twitch
								Creeper creeper = EntityType.CREEPER.create(level);
								if (creeper != null) {
									CompoundTag tag = new CompoundTag();
									tag.putBoolean("powered", true);
									creeper.readAdditionalSaveData(tag);
									creeper.setTarget(player);

									creeper.getPersistentData().putBoolean("dropless", true);

									creeper.setPos(player.position());
									level.addFreshEntity(creeper);
								}

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_TWITCH.get());
								}
								success.set(true);
							}
							case 25 -> {
								// Coal
								player.setSecondsOnFire(100);
								player.setRemainingFireTicks(1000);
								player.setSharedFlagOnFire(true);

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_COAL.get());
								}
								success.set(true);
							}
							case 26 -> {
								// Iron
								summonEntity(player, level, EntityType.IRON_GOLEM, true, 1, null, 0, null);

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_IRON.get());
								}
								success.set(true);
							}
							case 27 -> {
								// Gold
								summonEntity(player, level, EntityType.PIGLIN_BRUTE, true, 7, null, 0,
										new ItemStack[]{new ItemStack(Items.GOLDEN_SWORD),
												new ItemStack(Items.GOLDEN_HELMET),
												new ItemStack(Items.GOLDEN_CHESTPLATE)});

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_GOLD.get());
								}
								success.set(true);
							}
							case 28 -> {
								// Mod
								Collections.shuffle(player.getInventory().items);

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_MOD.get());
								}
								success.set(true);
							}
							case 29 -> {
								// Port
								for (int i = 0; i < 16; ++i) {
									double d3 = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;
									double d4 = Mth.clamp(player.getY() + (double) (player.getRandom().nextInt(16) - 8), (double) level.getMinBuildHeight(), (double) (level.getMinBuildHeight() + ((ServerLevel) level).getLogicalHeight() - 1));
									double d5 = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;
									if (player.isPassenger()) {
										player.stopRiding();
									}

									Vec3 vec3 = player.position();
									level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(player));
									net.minecraftforge.event.entity.EntityTeleportEvent.ChorusFruit event = net.minecraftforge.event.ForgeEventFactory.onChorusFruitTeleport(player, d3, d4, d5);
									if (player.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
										break;
									}
								}
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_PORT.get());
								}
								success.set(true);
							}
							case 30 -> {
								// Water
								player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, rand.nextInt(1200),
										rand.nextInt(3)));
								player.addEffect(new MobEffectInstance(RecogEffects.GRAVITY.get(), 1200, 0));

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_WATER.get());
								}
								success.set(true);
							}
							case 31 -> {
								// Block
								for (int i = 0; i < 7; i++) {
									Rabbit rabbit = EntityType.RABBIT.create(level);
									if (rabbit != null) {
										rabbit.setRabbitType(99);
										rabbit.setPos(player.position().add(rand.nextInt(3), 1, rand.nextInt(3)));
										rabbit.getPersistentData().putBoolean("dropless", true);
										level.addFreshEntity(rabbit);
									}
								}

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_BLOCK.get());
								}
								success.set(true);
							}
							case 32 -> {
								summonEntity(player, level, EntityType.WITCH, false, 4, MobEffects.INVISIBILITY, 0, null);

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_VILLAGE.get());
								}
								success.set(true);
							}
							case 33 -> {
								// Mine
								Item randItem = USELESS_ITEMS.get(rand.nextInt(USELESS_ITEMS.size()));
								giveItem(player, randItem, rand.nextInt(64));

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_MINE.get());
								}
								success.set(true);
							}
							case 34 -> {
								// Gam(e)
								Vec3 vec = player.position().add(randomOffset(10));
								level.explode(null, DamageSource.badRespawnPointExplosion(), null, vec.x, vec.y,
										vec.z, 5.0F, true, Explosion.BlockInteraction.DESTROY);

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_GAME.get());
								}
								success.set(true);
							}
							case 35 -> {
								// Light
								summonEntityOffset(player, level, EntityType.LIGHTNING_BOLT, false, 7, null, 0, null, 10);

								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_LIGHT.get());
								}
								success.set(true);
							}
							case 36 -> {
								// Ink
								player.getCapability(GraphicsTimersProvider.GRAPHICS_TIMERS).ifPresent(graphicsTimers -> {
									graphicsTimers.startSplat();
									RecogPacketHandler.sendToClient(new GraphicsTimersDataSyncPacket(graphicsTimers.getSplatTicks(), graphicsTimers.getTonyTicks()), player);
								});
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_INK.get());
								}
								success.set(true);
							}
							case 37 -> {
								// Bud
								// Handled clientside
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_BUD.get());
								}
								success.set(true);
							}
							case 38 -> {
								// Poggers
								player.getCapability(WordTimersProvider.WORD_TIMERS).ifPresent(wordTimers -> {
									if (wordTimers.getBeneficence() == 0) {
										player.heal(2);
										wordTimers.setMaxBeneficence(1200);
										wordTimers.addBeneficence(1200);
										RecogPacketHandler.sendToClient(new WordTimersDataSyncPacket(
												wordTimers.getBeneficence(), wordTimers.getMaxBeneficence(), wordTimers.getDisabledTime()), player);
									}
								});
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_POGGERS.get());
								}
								success.set(true);
							}
							case 39 -> {
								// Bless me papi
								player.getCapability(WordTimersProvider.WORD_TIMERS).ifPresent(wordTimers -> {
									if (wordTimers.getBeneficence() == 0 && wordTimers.getDisabledTime() == 0) {
										wordTimers.setDisabled();
										wordTimers.setMaxBeneficence(3600);
										wordTimers.addBeneficence(3600);
										RecogPacketHandler.sendToClient(new WordTimersDataSyncPacket(
												wordTimers.getBeneficence(), wordTimers.getMaxBeneficence(), wordTimers.getDisabledTime()), player);
									}
								});
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_BLESS.get());
								}
								success.set(true);
							}
							case 40 -> {
								// Thing
								player.getCapability(WordTimersProvider.WORD_TIMERS).ifPresent(wordTimers -> {
									if (wordTimers.getBeneficence() == 0) {
										giveItem(player, Items.IRON_NUGGET, 1);
										wordTimers.setMaxBeneficence(800);
										wordTimers.addBeneficence(800);
										RecogPacketHandler.sendToClient(new WordTimersDataSyncPacket(
												wordTimers.getBeneficence(), wordTimers.getMaxBeneficence(), wordTimers.getDisabledTime()), player);
									}
								});
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_THING.get());
								}
								success.set(true);
							}
							case 41 -> {
								// Godlike
								player.getCapability(WordTimersProvider.WORD_TIMERS).ifPresent(wordTimers -> {
									if (wordTimers.getBeneficence() == 0) {
										player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0));
										wordTimers.setMaxBeneficence(1200);
										wordTimers.addBeneficence(1200);
										RecogPacketHandler.sendToClient(new WordTimersDataSyncPacket(
												wordTimers.getBeneficence(), wordTimers.getMaxBeneficence(), wordTimers.getDisabledTime()), player);
									}
								});
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_GODLIKE.get());
								}
								success.set(true);
							}
							case 42 -> {
								// Tony
								player.getCapability(GraphicsTimersProvider.GRAPHICS_TIMERS).ifPresent(graphicsTimers -> {
									graphicsTimers.startTony();
									RecogPacketHandler.sendToClient(new GraphicsTimersDataSyncPacket(graphicsTimers.getSplatTicks(), graphicsTimers.getTonyTicks()), player);
								});
								if(player.equals(sp)) {
									player.awardStat(RecogStats.STAT_TONY.get());
								}
								success.set(true);
							}
							default -> {
							}
						}
					}
				}

		});
		ctx.get().setPacketHandled(true);
		return success.get();
	}
}
