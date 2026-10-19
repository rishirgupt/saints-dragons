package com.leon.saintsdragons.client.ui;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class VolitansBreathMeterIndicator {
    private static final Identifier WATER_GAUGE = Identifier.fromNamespaceAndPath(SaintsDragonsCommon.MOD_ID, "textures/gui/volitans/volitans_water_gauge.png");
    private static final Identifier POISON_GAUGE = Identifier.fromNamespaceAndPath(SaintsDragonsCommon.MOD_ID, "textures/gui/volitans/volitans_poison_gauge.png");
    private static final Identifier GAUGE_OVERLAY = Identifier.fromNamespaceAndPath(SaintsDragonsCommon.MOD_ID, "textures/gui/volitans/volitans_gauge_overlay.png");
    private static final Identifier GAUGE_OVERLAY_RED = Identifier.fromNamespaceAndPath(SaintsDragonsCommon.MOD_ID, "textures/gui/volitans/volitans_gauge_overlay_red.png");

    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 30;
    private static final float FILL_FALL_SPEED = 0.04F;
    private static final float FILL_RISE_SPEED = 0.02F;

    private float waterEnergy = 1.0F;
    private float poisonEnergy = 1.0F;
    private int breathMode = 0;
    private boolean breathing = false;

    private float previousAnimatedFill = 1.0F;
    private float animatedFill = 1.0F;
    private float redFlashAlpha = 0.0F;
    private float previousRedFlashAlpha = 0.0F;

    public void setWaterEnergy(float energy) {
        this.waterEnergy = clamp(energy, 0.0F, 1.0F);
    }

    public void setPoisonEnergy(float energy) {
        this.poisonEnergy = clamp(energy, 0.0F, 1.0F);
    }

    public void setBreathMode(int mode) {
        this.breathMode = mode <= 0 ? 0 : 1;
    }

    public void setBreathing(boolean breathing) {
        this.breathing = breathing;
    }

    public void tick() {
        previousAnimatedFill = animatedFill;
        previousRedFlashAlpha = redFlashAlpha;

        float targetFill = getSelectedEnergy();
        if (animatedFill < targetFill) {
            animatedFill = Math.min(targetFill, animatedFill + FILL_RISE_SPEED);
        } else if (animatedFill > targetFill) {
            animatedFill = Math.max(targetFill, animatedFill - FILL_FALL_SPEED);
        }

        if (targetFill < 0.25F && !breathing) {
            float pulse = (float) Math.sin((System.currentTimeMillis() % 1000L) / 1000.0F * Math.PI * 2.0F);
            redFlashAlpha = 0.5F + (pulse * 0.5F);
        } else {
            redFlashAlpha = 0.0F;
        }
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int screenWidth, int screenHeight, float partialTicks) {
        float clampedPartial = clamp(partialTicks, 0.0F, 1.0F);
        float smoothFill = lerp(previousAnimatedFill, animatedFill, clampedPartial);
        float smoothRedFlash = lerp(previousRedFlashAlpha, redFlashAlpha, clampedPartial);


        int x = (screenWidth - BAR_WIDTH) / 2;
        int y = screenHeight - 45;
        int fillWidth = Math.max(0, Math.min(BAR_WIDTH, Math.round(BAR_WIDTH * smoothFill)));

        Identifier activeGauge = breathMode == 1 ? POISON_GAUGE : WATER_GAUGE;
        if (fillWidth > 0) {
            com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, activeGauge, x, y, 0, 0, fillWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

        com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, GAUGE_OVERLAY, x, y, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        if (smoothRedFlash > 0.01F) {
            com.leon.saintsdragons.client.ui.Gui26.setColor(1.0F, 1.0F, 1.0F, smoothRedFlash);
            com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, GAUGE_OVERLAY_RED, x, y, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

        com.leon.saintsdragons.client.ui.Gui26.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private float getSelectedEnergy() {
        return breathMode == 1 ? poisonEnergy : waterEnergy;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}