package com.mco.mcrecog;

import net.minecraftforge.common.ForgeConfigSpec;

public class RecogConfig {
	public static final ForgeConfigSpec GENERAL_SPEC;

	static {
		ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
		setupConfig(configBuilder);
		GENERAL_SPEC = configBuilder.build();
	}

	public static ForgeConfigSpec.BooleanValue waterWhenSpawning;

	public static ForgeConfigSpec.BooleanValue displayDeathCount;
	public static ForgeConfigSpec.IntValue deathCountX;
	public static ForgeConfigSpec.IntValue deathCountY;
	public static ForgeConfigSpec.DoubleValue deathCountScale;

	private static void setupConfig(ForgeConfigSpec.Builder builder) {
		waterWhenSpawning = builder
				.comment("Whether to give water effect when spawning mobs")
				.define("water_spawns", true);

		builder.comment("This category deals with the displayed death counter");
		builder.push("Death Counter Options");
			displayDeathCount = builder
					.comment("Whether to display the death counter")
					.define("display_death", true);
			deathCountX = builder
					.comment("X position of the death counter on the screen")
					.defineInRange("death_count_x", 960, 0, 1920);
			deathCountY = builder
					.comment("Y position of the death counter on the screen")
					.defineInRange("death_count_y", 360, 0, 1080);
			deathCountScale = builder
					.comment("Scale of the death counter on the screen")
					.defineInRange("death_count_scale", 3.0D, 1.0D, 4.0D);
		builder.pop();
	}
}
