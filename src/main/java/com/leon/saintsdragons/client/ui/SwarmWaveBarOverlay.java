package com.leon.saintsdragons.client.ui;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class SwarmWaveBarOverlay {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            SaintsDragonsCommon.MOD_ID,
            "textures/gui/draconian_swarm/draconian_swarm_wave_bar.png"
    );
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int TEXTURE_HEIGHT = 11;
    private static final int FILL_V = 6;
    private static final int OVERLAY_V = 0;

    private static boolean active;
    private static int wave;
    private static int timeoutTicks;
    private static float targetProgress;
    private static float previousDisplayedProgress;
    private static float displayedProgress;

    private SwarmWaveBarOverlay() {
    }

    public static void signal(boolean active, int wave, float progress, int durationTicks) {
        SwarmWaveBarOverlay.active = active;
        SwarmWaveBarOverlay.wave = wave;
        SwarmWaveBarOverlay.timeoutTicks = Math.max(0, durationTicks);
        SwarmWaveBarOverlay.targetProgress = Mth.clamp(progress, 0.0F, 1.0F);
        if (!active) {
            SwarmWaveBarOverlay.targetProgress = 0.0F;
        }
    }

    public static void tick() {
        previousDisplayedProgress = displayedProgress;
        if (timeoutTicks > 0) {
            timeoutTicks--;
        } else {
            active = false;
            targetProgress = 0.0F;
        }
        displayedProgress += (targetProgress - displayedProgress) * 0.12F;
        if (!active && displayedProgress < 0.01F) {
            displayedProgress = 0.0F;
            previousDisplayedProgress = 0.0F;
        }
    }

    public static void render(GuiGraphicsExtractor graphics, int screenWidth, float partialTick) {
        float smoothProgress = Mth.lerp(Mth.clamp(partialTick, 0.0F, 1.0F),
                previousDisplayedProgress,
                displayedProgress);
        if (!active && smoothProgress <= 0.0F) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        int x = (screenWidth - BAR_WIDTH) / 2;
        int y = 12;
        Component title = wave > 0
                ? Component.translatable("gui.saintsdragons.draconian_swarm.wave", wave)
                : Component.translatable("gui.saintsdragons.draconian_swarm");
        int titleWidth = minecraft.font.width(title);

        com.leon.saintsdragons.client.ui.Gui26.text(graphics, minecraft.font, title, (screenWidth - titleWidth) / 2, y, 0xFFFFFFFF, true);

        int barY = y + 12;
        com.leon.saintsdragons.client.ui.Gui26.blit(graphics, TEXTURE, x, barY, 0, OVERLAY_V, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, TEXTURE_HEIGHT);
        int fillWidth = Mth.clamp(Math.round(BAR_WIDTH * smoothProgress), 0, BAR_WIDTH);
        if (fillWidth > 0) {
            com.leon.saintsdragons.client.ui.Gui26.blit(graphics, TEXTURE, x, barY, 0, FILL_V, fillWidth, BAR_HEIGHT, BAR_WIDTH, TEXTURE_HEIGHT);
        }
    }
}
