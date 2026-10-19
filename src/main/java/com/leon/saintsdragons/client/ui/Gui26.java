package com.leon.saintsdragons.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * Drawing helpers that keep the mod's 1.20.1-style GUI calls working on the 26.2 GUI pipeline.
 *
 * Two things changed underneath: textures and text are drawn with an explicit ARGB colour instead of the
 * global shader colour, and a colour without alpha is fully transparent. {@link #setColor} emulates the old
 * global tint for the blit helpers here, and the text helpers treat alpha 0 as opaque like 1.20.1 did.
 */
public final class Gui26 {
    private static int tint = 0xFFFFFFFF;

    /** Replacement for RenderSystem.setShaderColor: tints subsequent {@link #blit} calls. */
    public static void setColor(float red, float green, float blue, float alpha) {
        tint = ARGB.colorFromFloat(clamp(alpha), clamp(red), clamp(green), clamp(blue));
    }

    public static void resetColor() {
        tint = 0xFFFFFFFF;
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static int opaque(int color) {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    // --- textures (argument orders match the 1.20.1 GuiGraphics.blit overloads) ---

    /** blit(texture, x, y, u, v, width, height) on a 256x256 texture. */
    public static void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int u, int v,
                            int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, 256, 256, tint);
    }

    /** blit(texture, x, y, u, v, width, height, textureWidth, textureHeight). */
    public static void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, float u, float v,
                            int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth,
                textureHeight, tint);
    }

    /** blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight). */
    public static void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height,
                            float u, float v, int regionWidth, int regionHeight, int textureWidth,
                            int textureHeight) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, regionWidth,
                regionHeight, textureWidth, textureHeight, tint);
    }

    // --- text ---

    public static void text(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color,
                            boolean shadow) {
        graphics.text(font, text, x, y, opaque(color), shadow);
    }

    public static void text(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color,
                            boolean shadow) {
        graphics.text(font, text, x, y, opaque(color), shadow);
    }

    public static void text(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y,
                            int color, boolean shadow) {
        graphics.text(font, text, x, y, opaque(color), shadow);
    }

    public static void text(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
        graphics.text(font, text, x, y, opaque(color), true);
    }

    public static void text(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
        graphics.text(font, text, x, y, opaque(color), true);
    }

    public static void text(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y,
                            int color) {
        graphics.text(font, text, x, y, opaque(color), true);
    }

    public static void centeredText(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
        graphics.centeredText(font, text, x, y, opaque(color));
    }

    public static void centeredText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y,
                                    int color) {
        graphics.centeredText(font, text, x, y, opaque(color));
    }

    public static void centeredText(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x,
                                    int y, int color) {
        graphics.centeredText(font, text, x, y, opaque(color));
    }

    public static void wordWrap(GuiGraphicsExtractor graphics, Font font, FormattedText text, int x, int y,
                                int width, int color) {
        graphics.textWithWordWrap(font, text, x, y, width, opaque(color), false);
    }

    // --- 2D pose (the GUI matrix stack is 2D now; the old z component only ordered layers) ---

    public static void translate(GuiGraphicsExtractor graphics, double x, double y, double z) {
        graphics.pose().translate((float) x, (float) y);
    }

    public static void scale(GuiGraphicsExtractor graphics, double x, double y, double z) {
        graphics.pose().scale((float) x, (float) y);
    }

    public static void item(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        graphics.item(stack, x, y);
    }

    private Gui26() {
    }
}
