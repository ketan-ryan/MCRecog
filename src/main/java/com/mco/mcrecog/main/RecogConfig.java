package com.mco.mcrecog.main;

import net.minecraftforge.common.ForgeConfigSpec;

public class RecogConfig {
	public static final ForgeConfigSpec GENERAL_SPEC;
	public static final ForgeConfigSpec SERVER_SPEC;

	static {
		ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
		setupClientConfig(configBuilder);
		GENERAL_SPEC = configBuilder.build();

		ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();
		setupServerConfig(serverBuilder);
		SERVER_SPEC = serverBuilder.build();
	}

	public static ForgeConfigSpec.BooleanValue affectTeam;

	private static void setupServerConfig(ForgeConfigSpec.Builder builder) {
		affectTeam = builder
				.comment("Whether saying a word affects all players on a team")
				.define("affect_team", true);
	}

	public static ForgeConfigSpec.BooleanValue waterWhenSpawning;

	public static ForgeConfigSpec.BooleanValue displayDeathCount;
	public static ForgeConfigSpec.IntValue deathCountX;
	public static ForgeConfigSpec.IntValue deathCountY;
	public static ForgeConfigSpec.DoubleValue deathCountScale;

	private static void setupClientConfig(ForgeConfigSpec.Builder builder) {
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
					.defineInRange("death_count_x", 0, -1920, 1920);
			deathCountY = builder
					.comment("Y position of the death counter on the screen")
					.defineInRange("death_count_y", 0, -1080, 1080);
			deathCountScale = builder
					.comment("Scale of the death counter on the screen")
					.defineInRange("death_count_scale", 2.0D, 1.0D, 6.0D);
		builder.pop();
	}
}
