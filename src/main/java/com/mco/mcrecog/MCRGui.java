package com.mco.mcrecog;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;

import static com.mco.mcrecog.MCRUtils.SPLAT_TICKS;

public class MCRGui extends Gui {
    public MCRGui(Minecraft mc) {
        super(mc);
    }

    public final IIngameOverlay INKSPLAT_ELEMENT = OverlayRegistry.registerOverlayTop("Inksplat", (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
        gui.setupOverlayRenderState(true, false);
        this.renderInk(mStack);
    });

    private void renderInk(PoseStack inkStack) {
        Minecraft minecraft = Minecraft.getInstance();
        inkStack.pushPose();
        final int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        final int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        float widthRatio = screenWidth / 1920.0f;
        float heightRatio = screenHeight / 1080.0f;
        inkStack.scale(widthRatio, heightRatio, 1.0f);

        minecraft.getProfiler().push("inkBlot");
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        int ticks = minecraft.player.getPersistentData().getInt("splatTicks");
        float f = -1;
        if (ticks > 0)
            f = (float)Math.min(ticks, SPLAT_TICKS) / (float)SPLAT_TICKS;

        RenderSystem.setShaderColor(1f, 1f, 1f, f);
        RenderSystem.setShaderTexture(0, new ResourceLocation(McRecog.MODID, "textures/splat.png"));
                                                                                    // How much of the screen to draw on
                                                                                                                // Scale to draw the image at
                                                                                                                // If less than screen size will tile
        GuiComponent.blit(inkStack, 0, 0, 0, 0, 1920, 1080, 1920, 1080);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getProfiler().pop();
        inkStack.popPose();
    }
}
