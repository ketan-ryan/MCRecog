package com.mco.mcrecog.entities.corpse;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class CorpseRenderer extends EntityRenderer<Corpse> {
	private static final Minecraft MC = Minecraft.getInstance();

	public CorpseRenderer(EntityRendererProvider.Context renderer) {
		super(renderer);
	}

	@Override
	public ResourceLocation getTextureLocation(Corpse p_114482_) {
		return null;
	}

	@Override
	public void render(Corpse entity, float yaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLightIn) {
		super.render(entity, yaw, partialTicks, matrixStack, buffer, packedLightIn);
		matrixStack.pushPose();

		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90F));
		matrixStack.translate(0D, -1D, 2.01D / 16D);

		AbstractClientPlayer abstractClientPlayer;

		abstractClientPlayer = new RemotePlayer((ClientLevel) entity.level,
				new GameProfile(entity.getCorpseUUID().orElse(new UUID(0L, 0L)), entity.getCorpseName()), null);

		MC.getEntityRenderDispatcher().getRenderer(abstractClientPlayer).render(abstractClientPlayer, 0F, 1F, matrixStack, buffer, packedLightIn);
		matrixStack.popPose();
	}
}
