package com.mco.mcrecog.client;

import com.mco.mcrecog.MCRecog;
import com.mco.mcrecog.RecogConfig;
import com.mco.mcrecog.RecogUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.math.BigDecimal;

import static com.mco.mcrecog.RecogUtils.TONY_TICKS;

public class RecogGui {
	private static final ResourceLocation BAR = new ResourceLocation(MCRecog.MODID, "textures/mcr_icons.png");
	private static final ResourceLocation INK = new ResourceLocation(MCRecog.MODID, "textures/splat.png");
	private static final ResourceLocation TONY = new ResourceLocation(MCRecog.MODID, "textures/tony.png");

	public static final IGuiOverlay HUD_TONY = ((gui, poseStack, partialTick, screenWidth, screenHeight) -> {
		if(ClientGraphicsTimersData.getTonyTicks() >= TONY_TICKS) return;

		float widthRatio = screenWidth / 232.0F;
		float heightRatio = screenHeight / 321.0F;
		poseStack.pushPose();
		poseStack.translate(screenWidth / 4.0F, screenHeight / 4.0F, 0);
		poseStack.scale(widthRatio / 2.0F, heightRatio / 2.0F, 1.0F);
		int ticks = ClientGraphicsTimersData.getTonyTicks();
		if(ticks > 30) {
			poseStack.translate(0F, screenHeight / 2.0F, 0F);
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(ticks * 50));
		}
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TONY);
		GuiComponent.blit(poseStack, 0, 0, 0, 0, 232, 321, 232, 321);
		poseStack.popPose();
	});

	public static final IGuiOverlay HUD_INK = ((gui, poseStack, partialTick, screenWidth, screenHeight) -> {
		if(ClientGraphicsTimersData.getInkSplatTicks() <= 0) return;

		float widthRatio = screenWidth / 1920.0F;
		float heightRatio = screenHeight / 1080.0F;

		poseStack.pushPose();
		poseStack.scale(widthRatio, heightRatio, 1.0F);
		int ticks = ClientGraphicsTimersData.getInkSplatTicks();
		float f = 1.0F;
		if(ticks < RecogUtils.SPLAT_START) {
			f = (float) ticks / (float) RecogUtils.SPLAT_START;
		}
		RenderSystem.enableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);

		RenderSystem.setShaderTexture(0, INK);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f);
		GuiComponent.blit(poseStack, 0, 0, 0, 0, 1920, 1080, 1920, 1080);

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		poseStack.popPose();
	});

	public static final IGuiOverlay HUD_DEATHS = ((gui, poseStack, partialTick, screenWidth, screenHeight) -> {
		Font font = Minecraft.getInstance().font;
		String overlayMessageString = "Deaths: " + ClientDeathData.getDeaths();

		int x = 4 + RecogConfig.deathCountX.get();
		int y = (screenHeight / 6) + RecogConfig.deathCountY.get();

		poseStack.pushPose();
		float scale = BigDecimal.valueOf(RecogConfig.deathCountScale.get()).floatValue();
		poseStack.scale(scale, scale, 1.0F);
		GuiComponent.drawString(poseStack, font, overlayMessageString, x, y, 16777215);
		poseStack.popPose();
	});

	public static final IGuiOverlay HUD_BARS = ((gui, poseStack, partialTick, screenWidth, screenHeight) -> {
		int x = (screenWidth / 2) - 92;
		int y = screenHeight - 50;

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, BAR);

		boolean isBeneficenceActive = ClientBeneficenceData.getPlayerBeneficence() > 0;
		boolean isDisabled = ClientWordsDisabledData.getWordsDisabledTime() > 0;

		if(isBeneficenceActive) {
			//               stack       x  y      texX        texY         width      height     texWidth    texHeight
			GuiComponent.blit(poseStack, x, y, 0, 0, 97, 5, 256, 256);
			RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 1.0F);
			float f = (float) ClientBeneficenceData.getPlayerBeneficence() / (float) ClientBeneficenceData.getMaxBeneficence();
			int j = (int) (f * 97.0F); // where 97 is the width of the bar
			if (j > 0) {
				GuiComponent.blit(poseStack, x, y, 0, 5, j, 5, 256, 256);
			}
		}

		int disabledYOff = isBeneficenceActive ? y - 6 : y;
		if(isDisabled) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			GuiComponent.blit(poseStack, x, disabledYOff, 0, 0, 97, 5, 256, 256);
			RenderSystem.setShaderColor(1.0F, 0.0F, 0.0F, 1.0F);
			float f = (float) ClientWordsDisabledData.getWordsDisabledTime() / 800.0F;
			int j = (int) (f * 97.0F); // where 97 is the width of the bar
			if (j > 0) {
				GuiComponent.blit(poseStack, x, disabledYOff, 0, 5, j, 5, 256, 256);
			}
		}
	});
}
