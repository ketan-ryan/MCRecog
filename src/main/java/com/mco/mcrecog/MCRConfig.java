package com.mco.mcrecog;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class MCRConfig {
    public static class Common {
        public final ForgeConfigSpec.IntValue debugLevel;

        public final ForgeConfigSpec.IntValue deathCountX;
        public final ForgeConfigSpec.IntValue deathCountY;

        public final ForgeConfigSpec.BooleanValue waterWhenSpawning;

        Common(final ForgeConfigSpec.Builder builder) {
            builder.comment("Config settings").push("common");

            debugLevel = builder
                    .comment("Debug level of comments: 0 none, 1 only highlighted word, 2 all voice inputs")
                    .defineInRange("debug_level", 0, 0, 2);

            deathCountX = builder
                    .comment("X position (in pixels) of the death counter on your screen")
                    .defineInRange("death_count_x", 960, 0, 1920);
            deathCountY = builder
                    .comment("Y position (in pixels) of the death counter on your screen")
                    .defineInRange("death_count_y", 360, 0, 1080);

            waterWhenSpawning = builder
                    .comment("Whether to give water effect when spawning mobs")
                    .define("water_spawns", true);
        }
    }
    private static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static void register(final ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, commonSpec);
    }
}
