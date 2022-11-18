package com.mco.mcrecog.client;

import com.mco.mcrecog.MCRecog;
import com.mco.mcrecog.RecogConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.math.BigDecimal;

public class RecogGui {
	private static final ResourceLocation BAR = new ResourceLocation(MCRecog.MODID, "textures/mcr_icons.png");

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
