package com.mco.mcrecog.effects;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class GravityEffect extends MobEffect {
	protected GravityEffect(final MobEffectCategory effectType, final int liquidColor) {
		super(effectType, liquidColor);
	}

	public GravityEffect(final MobEffectCategory effectType, final int liquidR, final int liquidG, final int liquidB) {
		this(effectType, new Color(liquidR, liquidG, liquidB).getRGB());
	}

	@Override
	public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
		return true;
	}

	@Override
	public void applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
		super.applyEffectTick(livingEntity, amplifier);
		if (livingEntity.level.isClientSide() && livingEntity instanceof LocalPlayer p) {
			if(p.isSpectator() || p.isCreative()) return;

			Vec3 vec = livingEntity.getDeltaMovement();
			livingEntity.setDeltaMovement(vec.x, -0.3D * (1 + amplifier), vec.z);
			livingEntity.resetFallDistance();
		}
	}
}
