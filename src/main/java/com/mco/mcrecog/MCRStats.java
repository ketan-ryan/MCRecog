package com.mco.mcrecog;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

/**
 * Create some custom stats about how many times a player has said a certain word
 */
public class MCRStats {
    public static final ResourceLocation NO_SHOT = registerCustomStat("no_shot");
    public static final ResourceLocation BEAR = registerCustomStat("bear");
    public static final ResourceLocation AXOLOTL = registerCustomStat("axolotl");
    public static final ResourceLocation ROT = registerCustomStat("rot");
    public static final ResourceLocation BONE = registerCustomStat("bone");
    public static final ResourceLocation PIG = registerCustomStat("pig");
    public static final ResourceLocation SUB = registerCustomStat("sub");
    public static final ResourceLocation CREEP = registerCustomStat("creep");
    public static final ResourceLocation ROD = registerCustomStat("rod");
    public static final ResourceLocation END = registerCustomStat("end");
    public static final ResourceLocation NETHER = registerCustomStat("nether");
    public static final ResourceLocation CAVE = registerCustomStat("cave");
    public static final ResourceLocation FOLLOW = registerCustomStat("follow");
    public static final ResourceLocation DAY = registerCustomStat("day");
    public static final ResourceLocation BED = registerCustomStat("bed");
    public static final ResourceLocation DRAGON = registerCustomStat("dragon");
    public static final ResourceLocation TWITCH = registerCustomStat("twitch");
    public static final ResourceLocation COAL = registerCustomStat("coal");
    public static final ResourceLocation IRON = registerCustomStat("iron");
    public static final ResourceLocation GOLD = registerCustomStat("gold");
    public static final ResourceLocation DIAMOND = registerCustomStat("diamond");
    public static final ResourceLocation MOD = registerCustomStat("mod");
    public static final ResourceLocation PORT = registerCustomStat("port");
    public static final ResourceLocation WATER = registerCustomStat("water");
    public static final ResourceLocation BLOCK = registerCustomStat("block");
    public static final ResourceLocation UP = registerCustomStat("up");
    public static final ResourceLocation CRAFT = registerCustomStat("craft");
    public static final ResourceLocation VILLAGE = registerCustomStat("village");
    public static final ResourceLocation MINE = registerCustomStat("mine");
    public static final ResourceLocation GAME = registerCustomStat("game");
    public static final ResourceLocation LIGHT = registerCustomStat("light");

    /**
     * Register the stat
     * @param name the name of the stat to register
     * @return the ResourceLocation of the stat
     */
    private static ResourceLocation registerCustomStat(String name) {
        ResourceLocation resourcelocation = new ResourceLocation(McRecog.MODID, name);
        Registry.register(Registry.CUSTOM_STAT, name, resourcelocation);
        Stats.CUSTOM.get(resourcelocation, StatFormatter.DEFAULT);
        return resourcelocation;
    }
}
