package com.leon.saintsdragons.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

/**
 * Button that draws a plain texture file. Vanilla's ImageButton only accepts GUI atlas sprites since 1.20.2;
 * the mod's icon buttons use standalone textures.
 */
public class TextureIconButton extends Button {
    private final Identifier texture;
    private final int textureWidth;
    private final int textureHeight;

    public TextureIconButton(int x, int y, int width, int height, Identifier texture, int textureWidth,
                             int textureHeight, OnPress onPress, net.minecraft.network.chat.Component message) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public TextureIconButton(int x, int y, int width, int height, Identifier texture, int textureWidth,
                             int textureHeight, OnPress onPress) {
        super(x, y, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Gui26.resetColor();
        Gui26.blit(graphics, texture, getX(), getY(), 0.0F, 0.0F, width, height, textureWidth, textureHeight);
    }
}
