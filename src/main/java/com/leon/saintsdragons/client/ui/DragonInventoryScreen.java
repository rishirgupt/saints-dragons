package com.leon.saintsdragons.client.ui;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.menu.DragonInventoryMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DragonInventoryScreen extends AbstractContainerScreen<DragonInventoryMenu> {
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int CHEST_SLOTS_U = 0;
    private static final int CHEST_SLOTS_V = 166;
    private static final int CHEST_SLOT_U = 0;
    private static final int CHEST_SLOT_V = 220;
    private static final int SADDLE_SLOT_U = 18;
    private static final int SADDLE_SLOT_V = 220;
    private static final int PREVIEW_FRAME_X = 26;
    private static final int PREVIEW_FRAME_Y = 18;
    private static final int PREVIEW_FRAME_SIZE = 52;
    private static final int PREVIEW_OFFSET_X = 51;
    private static final int PREVIEW_OFFSET_Y = 60;
    private static final int PREVIEW_SCALE = 10;
    private static final int PREVIEW_MOUSE_Y_OFFSET = 24;

    private static final Identifier TEXTURE =
            SaintsDragonsCommon.rl("textures/gui/dragon_inventory_gui.png");
    @Nullable
    private final LivingEntity dragon;

    public DragonInventoryScreen(DragonInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
        Entity vehicle = inventory.player.getVehicle();
        this.dragon = vehicle instanceof LivingEntity living ? living : null;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        com.leon.saintsdragons.client.ui.Gui26.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = this.leftPos;
        int y = this.topPos;
        com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, TEXTURE, x + 7, y + 17, SADDLE_SLOT_U, SADDLE_SLOT_V, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, TEXTURE, x + 7, y + 39, CHEST_SLOT_U, CHEST_SLOT_V, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        if (this.menu.hasChestInstalled()) {
            com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, TEXTURE, x + 79, y + 17, CHEST_SLOTS_U, CHEST_SLOTS_V,
                    this.menu.getChestColumns() * 18, this.menu.getChestRows() * 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        if (this.dragon != null) {
            int renderX = x + PREVIEW_OFFSET_X;
            int renderY = y + PREVIEW_OFFSET_Y;
            guiGraphics.enableScissor(
                    x + PREVIEW_FRAME_X,
                    y + PREVIEW_FRAME_Y,
                    x + PREVIEW_FRAME_X + PREVIEW_FRAME_SIZE,
                    y + PREVIEW_FRAME_Y + PREVIEW_FRAME_SIZE
            );
            InventoryScreen.extractEntityInInventoryFollowsMouse(
                    guiGraphics, (renderX) - 48, (renderY) - 80, (renderX) + 48, (renderY) + 8, (int) (PREVIEW_SCALE), 0.0625F, mouseX, mouseY, this.dragon);
            guiGraphics.disableScissor();
        }
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        this.extractTooltip(guiGraphics, mouseX, mouseY);
    }
}
