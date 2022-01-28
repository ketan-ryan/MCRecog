package com.mco.mcrecog;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
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

    public final IIngameOverlay TONY_ELEMENT = OverlayRegistry.registerOverlayTop("Tony", (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
        gui.setupOverlayRenderState(true, false);
        this.drawTony(mStack);
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

    private void drawTony(PoseStack tonyStack) {

        Minecraft minecraft = Minecraft.getInstance();
        int ticks = minecraft.player.getPersistentData().getInt("tony");
        if (ticks == 0) return;
        tonyStack.pushPose();
        final int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        final int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        float widthRatio = screenWidth / 232.0f;
        float heightRatio = screenHeight / 320.0f;
//        tonyStack.scale(widthRatio, heightRatio, 1.0f);
        tonyStack.scale(.5F, .5F, 1F);
//        tonyStack.translate(screenWidth - 120, heightRatio + 380, 0D);
        tonyStack.translate(screenWidth, heightRatio + 480, 0D);
        if (ticks > 40)
            tonyStack.mulPose(Vector3f.ZP.rotationDegrees(ticks * 50));
        else {
            tonyStack.scale(2F, 2F, 1F);
            tonyStack.translate(-125, -125, 0D);
        }
        minecraft.getProfiler().push("tony");
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, new ResourceLocation(McRecog.MODID, "textures/tony.png"));
        GuiComponent.blit(tonyStack, 0, 0, 0, 0, 232, 320, 232, 320);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getProfiler().pop();
        tonyStack.popPose();
    }

    public static void renderBar(String profilerTag, String desiredData, int max, int yOff, float r, float g, float b) {
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.player.getPersistentData().getInt(desiredData) <= 0) return;

        PoseStack barStack = new PoseStack();
        barStack.pushPose();
        barStack.scale(1.5F, 1.5F, 1.5F);
        minecraft.getProfiler().push(profilerTag);
        RenderSystem.setShaderColor(r, g, b, 1F);
        RenderSystem.setShaderTexture(0, new ResourceLocation(McRecog.MODID, "textures/mcr_icons.png"));
        float f = minecraft.player.getPersistentData().getInt(desiredData) / (float) max;
        int j = (int) (f * 97.0F); // Where 97 is the width of the bar
        int y = MCRConfig.COMMON.deathCountY.get() - yOff;
        int x = MCRConfig.COMMON.deathCountX.get() - 87;
        // PoseStack         Position on Screen   Tex x      Tex Y       bar width    Bar height   Tex width      Tex white
        GuiComponent.blit(barStack, x, y, 0, 0, 97, 5, 256, 256);
        if (j > 0) // If we have progress
            GuiComponent.blit(barStack, x, y, 0, 5, j, 5, 256, 256);
        minecraft.getProfiler().pop();
        barStack.popPose();
    }
}
