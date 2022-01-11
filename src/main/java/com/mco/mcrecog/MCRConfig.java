package com.mco.mcrecog;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class MCRConfig {
    public static class Common {
        public final ForgeConfigSpec.IntValue debugLevel;

        Common(final ForgeConfigSpec.Builder builder) {
            builder.comment("Config settings").push("common");

            debugLevel = builder
                    .comment("Debug level of comments: 0 none, 1 only highlighted word, 2 all voice inputs")
                    .defineInRange("debug_level", 0, 0, 2);
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
