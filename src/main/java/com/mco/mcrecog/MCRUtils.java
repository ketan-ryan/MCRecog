package com.mco.mcrecog;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

public class MCRUtils {
    private static final Random rand = new Random();

    /**
     * Method to summon any amount of entities with an optional target and set of potion effects
     * @param player The player whose position to spawn the entities at
     * @param level The instance ofLevel to which the player belongs
     * @param e The EntityType to create
     * @param hostile Whether the mob is a neutral mob that needs to be set hostile to the player
     * @param count The amount of mobs to spawn
     * @param effect The optional potion effect to spawn with
     * @param strength The strength of the effect
     * @param stacks The optional list of ItemStacks to be equipped with
     */
    public static void summonEntity(Player player, Level level, EntityType<? extends LivingEntity> e, boolean hostile, int count,
                      MobEffect effect, int strength, ItemStack[] stacks) {
        for(int i = 0; i < count; i++) {
            LivingEntity entity = e.create(level);
            if(entity != null) {
                entity.setPos(player.position());

                // Call setTarget if applicable
                if(hostile) {
                    if (entity instanceof Mob mob)
                        mob.setTarget(player);
                    if(entity instanceof NeutralMob mob)
                        mob.setTarget(player);
                }
                // Spawn with potion effects if applicable
                if(effect != null)
                    entity.addEffect(new MobEffectInstance(effect, Integer.MAX_VALUE, strength));
                // Equip with items if applicable
                if(stacks != null && entity instanceof Monster monster) {
                    for (ItemStack stack : stacks)
                        monster.equipItemIfPossible(stack);
                }
                // Add a custom NBT tag to prevent mobs from dropping anything
                entity.getPersistentData().putBoolean("dropless", true);
                level.addFreshEntity(entity);
            }
        }
    }

    /**
     * Clears a space above the player's head so tall mobs don't suffocate
     * @param player the player to clear the blocks above
     * @param level the server level instance
     */
    public static void clearBlocksAbove(Player player, Level level) {
        BlockPos pos = player.blockPosition();
        for(int i = 0; i < 4; i ++) {
            level.setBlock(pos.north().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.west().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.south().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.east().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.north().east().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.south().east().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.north().west().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.south().west().offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pos.offset(0, i, 0), Blocks.AIR.defaultBlockState(), 2);
        }
    }

    /**
     * Removes a random amount of a random, non-empty item in the player's inventory
     * @param player the player to remove items from
     */
    public static void removeRandomItem(Player player) {
        if (player.getInventory().items.size() == 0)
            return;

        int slotId = rand.nextInt(player.getInventory().items.size());

        while(player.getInventory().getItem(slotId).equals(ItemStack.EMPTY))
            slotId = rand.nextInt(player.getInventory().items.size());

        int slotCount = player.getInventory().getItem(slotId).getCount() + 1;
        int c = rand.nextInt(slotCount + 1);

        System.out.println("Removing " + c + " of item " + player.getInventory().getItem(slotId));
        player.getInventory().removeItem(slotId, c);
    }
}
