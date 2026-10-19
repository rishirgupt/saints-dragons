package com.leon.saintsdragons.client.ui.codex;

import com.leon.saintsdragons.server.entity.handler.DragonAllyManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Consumer;

public class CodexAllyPanel {
    private final Identifier editBoxTexture;
    private final Identifier addIcon;
    private final Identifier removeIcon;
    private CustomEditBox allyInput;
    private net.minecraft.client.gui.components.Button addAllyButton;
    private net.minecraft.client.gui.components.Button removeAllyButton;

    public CodexAllyPanel(Identifier editBoxTexture, Identifier addIcon, Identifier removeIcon) {
        this.editBoxTexture = editBoxTexture;
        this.addIcon = addIcon;
        this.removeIcon = removeIcon;
    }

    public void initWidgets(Consumer<AbstractWidget> addWidget, Font font,
                            int leftPos, int topPos, Consumer<String> addCallback, Consumer<String> removeCallback) {
        int inputX = leftPos + 233;
        int inputY = topPos + 17;
        int inputWidth = 122;
        int inputHeight = 12;

        allyInput = new CustomEditBox(
                font,
                inputX,
                inputY,
                inputWidth,
                inputHeight,
                Component.translatable("saintsdragons.gui.draconic_codex.ally.input")
        );
        allyInput.setMaxLength(16);
        allyInput.setBordered(false);
        allyInput.setTextColor(CodexLayout.TEXT_COLOR);
        allyInput.setTextColorUneditable(CodexLayout.TEXT_COLOR);
        addWidget.accept(allyInput);

        int iconButtonX = inputX + inputWidth + 8;

        addAllyButton = new com.leon.saintsdragons.client.ui.TextureIconButton(iconButtonX - 36, inputY + 18, 16, 16, addIcon, 16, 16, button -> addAllyFromInput(addCallback)
        );
        addAllyButton.setTooltip(Tooltip.create(
                Component.translatable("saintsdragons.gui.draconic_codex.ally.add")));
        addWidget.accept(addAllyButton);

        removeAllyButton = new com.leon.saintsdragons.client.ui.TextureIconButton(iconButtonX - 19, inputY + 18, 16, 16, removeIcon, 16, 16, button -> removeAllyFromInput(removeCallback)
        );
        removeAllyButton.setTooltip(Tooltip.create(
                Component.translatable("saintsdragons.gui.draconic_codex.ally.remove")));
        addWidget.accept(removeAllyButton);
    }

    public void updateVisibility(boolean show) {
        if (allyInput != null) {
            allyInput.setVisible(show);
            allyInput.setEditable(show);
        }
        if (addAllyButton != null) {
            addAllyButton.visible = show;
            addAllyButton.active = show;
        }
        if (removeAllyButton != null) {
            removeAllyButton.visible = show;
            removeAllyButton.active = show;
        }
    }

    public void draw(GuiGraphicsExtractor guiGraphics, Font font, int left, int top, int bottom,
                     List<String> allyList, int allyScrollOffset) {
        if (allyInput != null) {
            com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, editBoxTexture,
                    allyInput.getX() - 1,
                    allyInput.getY() - 2,
                    0, 0,
                    120, 16,
                    120, 16);
        }

        int contentX = left + 81;
        int contentY = top - 5;

        String allyCountText = Component.translatable(
                "saintsdragons.gui.draconic_codex.ally.count",
                allyList.size(),
                DragonAllyManager.getMaxAlliesStatic()
        ).getString();
        com.leon.saintsdragons.client.ui.Gui26.text(guiGraphics, font, allyCountText, contentX, contentY, CodexLayout.TEXT_COLOR, false);

        int listTop = contentY + 16;
        int listBottom = bottom - 16;
        int visibleCount = Math.min(CodexLayout.MAX_VISIBLE_ALLIES, allyList.size() - allyScrollOffset);
        if (visibleCount < 0) {
            visibleCount = 0;
        }

        for (int i = 0; i < visibleCount; i++) {
            int index = i + allyScrollOffset;
            if (index >= allyList.size()) {
                break;
            }
            int y = listTop + (i * 12);
            com.leon.saintsdragons.client.ui.Gui26.text(guiGraphics, font, allyList.get(index), contentX, y, CodexLayout.TEXT_COLOR, false);
        }

        if (allyList.isEmpty()) {
            com.leon.saintsdragons.client.ui.Gui26.text(guiGraphics, font,
                    Component.translatable("saintsdragons.gui.draconic_codex.ally.empty").getString(),
                    contentX, listTop, CodexLayout.TEXT_COLOR, false);
        }
    }

    private void addAllyFromInput(Consumer<String> addCallback) {
        if (allyInput == null) {
            return;
        }
        String username = allyInput.getValue().trim();
        if (username.isEmpty()) {
            return;
        }
        addCallback.accept(username);
        allyInput.setValue("");
    }

    private void removeAllyFromInput(Consumer<String> removeCallback) {
        if (allyInput == null) {
            return;
        }
        String username = allyInput.getValue().trim();
        if (username.isEmpty()) {
            return;
        }
        removeCallback.accept(username);
        allyInput.setValue("");
    }
}
