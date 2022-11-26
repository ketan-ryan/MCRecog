package com.mco.mcrecog.main;

import com.mco.mcrecog.MCRecog;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class RecogStats {
	private static final DeferredRegister<ResourceLocation> STATS_REGISTRY = DeferredRegister.create(Registry.CUSTOM_STAT_REGISTRY, MCRecog.MODID);
	private static boolean isInitialized;

	public static final RegistryObject<ResourceLocation> STAT_CROW = STATS_REGISTRY.register("crow", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "crow"));
	public static final RegistryObject<ResourceLocation> STAT_PIG = STATS_REGISTRY.register("pig", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "pig"));
	public static final RegistryObject<ResourceLocation> STAT_SUB = STATS_REGISTRY.register("sub", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "sub"));
	public static final RegistryObject<ResourceLocation> STAT_FOLLOW = STATS_REGISTRY.register("follow", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "follow"));
	public static final RegistryObject<ResourceLocation> STAT_CAVE = STATS_REGISTRY.register("cave", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "cave"));
	public static final RegistryObject<ResourceLocation> STAT_YIKE = STATS_REGISTRY.register("yike", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "yike"));
	public static final RegistryObject<ResourceLocation> STAT_DAY = STATS_REGISTRY.register("day", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "day"));
	public static final RegistryObject<ResourceLocation> STAT_TROLL = STATS_REGISTRY.register("troll", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "troll"));
	public static final RegistryObject<ResourceLocation> STAT_HIGH = STATS_REGISTRY.register("high", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "high"));
	public static final RegistryObject<ResourceLocation> STAT_DIAMOND = STATS_REGISTRY.register("diamond", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "diamond"));
	public static final RegistryObject<ResourceLocation> STAT_CRAFT = STATS_REGISTRY.register("craft", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "craft"));
	public static final RegistryObject<ResourceLocation> STAT_ROT = STATS_REGISTRY.register("rot", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "rot"));
	public static final RegistryObject<ResourceLocation> STAT_BONE = STATS_REGISTRY.register("bone", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "bone"));
	public static final RegistryObject<ResourceLocation> STAT_DREAM = STATS_REGISTRY.register("dream", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "dream"));
	public static final RegistryObject<ResourceLocation> STAT_END = STATS_REGISTRY.register("end", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "end"));
	public static final RegistryObject<ResourceLocation> STAT_DRAGON = STATS_REGISTRY.register("dragon", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "dragon"));
	public static final RegistryObject<ResourceLocation> STAT_BOAT = STATS_REGISTRY.register("boat", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "boat"));
	public static final RegistryObject<ResourceLocation> STAT_NO_SHOT = STATS_REGISTRY.register("no_shot", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "no_shot"));
	public static final RegistryObject<ResourceLocation> STAT_BEAR = STATS_REGISTRY.register("bear", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "bear"));
	public static final RegistryObject<ResourceLocation> STAT_AXOLOTL = STATS_REGISTRY.register("axolotl", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "axolotl"));
	public static final RegistryObject<ResourceLocation> STAT_CREEP = STATS_REGISTRY.register("creep", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "creep"));
	public static final RegistryObject<ResourceLocation> STAT_ROD = STATS_REGISTRY.register("rod", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "rod"));
	public static final RegistryObject<ResourceLocation> STAT_NETHER = STATS_REGISTRY.register("nether", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "nether"));
	public static final RegistryObject<ResourceLocation> STAT_BED = STATS_REGISTRY.register("bed", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "bed"));
	public static final RegistryObject<ResourceLocation> STAT_TWITCH = STATS_REGISTRY.register("twitch", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "twitch"));
	public static final RegistryObject<ResourceLocation> STAT_COAL = STATS_REGISTRY.register("coal", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "coal"));
	public static final RegistryObject<ResourceLocation> STAT_IRON = STATS_REGISTRY.register("iron", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "iron"));
	public static final RegistryObject<ResourceLocation> STAT_GOLD = STATS_REGISTRY.register("gold", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "gold"));
	public static final RegistryObject<ResourceLocation> STAT_MOD = STATS_REGISTRY.register("mod", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "mod"));
	public static final RegistryObject<ResourceLocation> STAT_PORT = STATS_REGISTRY.register("port", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "port"));
	public static final RegistryObject<ResourceLocation> STAT_WATER = STATS_REGISTRY.register("water", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "water"));
	public static final RegistryObject<ResourceLocation> STAT_BLOCK = STATS_REGISTRY.register("block", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "block"));
	public static final RegistryObject<ResourceLocation> STAT_VILLAGE = STATS_REGISTRY.register("village", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "village"));
	public static final RegistryObject<ResourceLocation> STAT_MINE = STATS_REGISTRY.register("mine", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "mine"));
	public static final RegistryObject<ResourceLocation> STAT_GAME = STATS_REGISTRY.register("game", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "game"));
	public static final RegistryObject<ResourceLocation> STAT_LIGHT = STATS_REGISTRY.register("light", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "light"));
	public static final RegistryObject<ResourceLocation> STAT_INK = STATS_REGISTRY.register("ink", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "ink"));
	public static final RegistryObject<ResourceLocation> STAT_BUD = STATS_REGISTRY.register("bud", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "bud"));
	public static final RegistryObject<ResourceLocation> STAT_POGGERS = STATS_REGISTRY.register("poggers", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "poggers"));
	public static final RegistryObject<ResourceLocation> STAT_BLESS = STATS_REGISTRY.register("bless", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "bless"));
	public static final RegistryObject<ResourceLocation> STAT_THING = STATS_REGISTRY.register("thing", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "thing"));
	public static final RegistryObject<ResourceLocation> STAT_GODLIKE = STATS_REGISTRY.register("godlike", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "godlike"));
	public static final RegistryObject<ResourceLocation> STAT_TONY = STATS_REGISTRY.register("tony", () -> new ResourceLocation(MCRecog.MODID,  "stat_" + "tony"));

	/**
	 * Registers the {@link DeferredRegister} instance with the mod event bus.
	 * <p>
	 * This should be called during mod construction.
	 *
	 * @param modEventBus The mod event bus
	 */
	public static void initialise(final IEventBus modEventBus) {
		if (isInitialized) {
			throw new IllegalStateException("Already initialised");
		}

		STATS_REGISTRY.register(modEventBus);
		isInitialized = true;
	}

}
