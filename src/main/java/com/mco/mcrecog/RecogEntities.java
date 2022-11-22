package com.mco.mcrecog;

import com.mco.mcrecog.entities.corpse.Corpse;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecogEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MCRecog.MODID);

	public static final RegistryObject<EntityType<Corpse>> CORPSE = ENTITIES.register("modid", () ->
			EntityType.Builder.of(Corpse::new, MobCategory.CREATURE)
					.sized(1.5F, 0.5F)
					.build(new ResourceLocation(MCRecog.MODID, "corpse").toString()));
}
