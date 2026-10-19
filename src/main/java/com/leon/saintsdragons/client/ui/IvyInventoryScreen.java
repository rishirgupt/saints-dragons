package com.leon.saintsdragons.client.ui;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.item.StegonautBinderItem;
import com.leon.saintsdragons.server.entity.npc.IvyTheDragonMerchant;
import com.leon.saintsdragons.server.menu.IvyInventoryMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class IvyInventoryScreen extends AbstractContainerScreen<IvyInventoryMenu> {
    private static final Identifier TEXTURE =
            SaintsDragonsCommon.rl("textures/gui/ivy/ivy_inventory.png");
    private static final int TEX_W = 256;
    private static final int TEX_H = 256;
    private static final int IMAGE_W = 176;
    private static final int IMAGE_H = 234;
    private static final int BASE_W = 176;
    private static final int PREVIEW_X = 51;
    private static final int PREVIEW_Y = 74;
    private static final int PREVIEW_SCALE = 28;
    private static final int PREVIEW_MOUSE_Y_OFFSET = 34;
    private static final int VITALS_DEST_ICON_X = 84;
    private static final int VITALS_DEST_BAR_X = 96;
    private static final int VITALS_DEST_HEALTH_Y = 11;
    private static final int VITALS_DEST_ARMOR_Y = 23;
    private static final int VITALS_VALUE_X = 162;
    private static final int VITALS_SOURCE_HEART_ICON_X = 177;
    private static final int VITALS_SOURCE_HEART_ICON_Y = 0;
    private static final int VITALS_SOURCE_ARMOR_ICON_X = 177;
    private static final int VITALS_SOURCE_ARMOR_ICON_Y = 12;
    private static final int VITALS_SOURCE_BAR_X = 187;
    private static final int VITALS_SOURCE_HEALTH_BAR_Y = 0;
    private static final int VITALS_SOURCE_ARMOR_BAR_Y = 12;
    private static final int VITALS_SOURCE_FILL_X = 190;
    private static final int VITALS_SOURCE_HEALTH_FILL_Y = 9;
    private static final int VITALS_SOURCE_ARMOR_FILL_Y = 21;
    private static final int ICON_SIZE = 9;
    private static final int BAR_W = 62;
    private static final int BAR_H = 8;
    private static final int FILL_W = 44;
    private static final int FILL_H = 2;
    private static final int EFFECT_ICON_SIZE = 18;
    private static final int EFFECT_ICON_GAP = 4;
    private static final int EFFECTS_LEFT_GAP = 24;
    private static final int EFFECTS_TOP_Y = 7;

    public IvyInventoryScreen(IvyInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, IMAGE_W, IMAGE_H);
        this.titleLabelX = 80;
        this.titleLabelY = 6;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 142;
    }

    @Override
    protected void extractLabels(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        com.leon.saintsdragons.client.ui.Gui26.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = this.leftPos;
        int y = this.topPos;
        com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, TEXTURE, x, y, 0, 0, BASE_W, IMAGE_H, TEX_W, TEX_H);
        renderIvyPreview(guiGraphics, x, y, mouseX, mouseY);
        renderVitals(guiGraphics, x, y);
    }

    private void renderIvyPreview(GuiGraphicsExtractor guiGraphics, int x, int y, int mouseX, int mouseY) {
        IvyTheDragonMerchant ivy = getIvy();
        if (ivy == null) {
            return;
        }
        int renderX = x + PREVIEW_X;
        int renderY = y + PREVIEW_Y;
        InventoryScreen.extractEntityInInventoryFollowsMouse(
                    guiGraphics, (renderX) - 48, (renderY) - 80, (renderX) + 48, (renderY) + 8, (int) (PREVIEW_SCALE), 0.0625F, mouseX, mouseY, ivy);
    }

    private void renderVitals(GuiGraphicsExtractor guiGraphics, int x, int y) {
        IvyTheDragonMerchant ivy = getIvy();
        int healthFillWidth = FILL_W;
        int healthValue = 0;
        if (ivy != null) {
            float healthRatio = Math.max(0.0F, Math.min(1.0F, ivy.getHealth() / ivy.getMaxHealth()));
            healthFillWidth = Math.round(FILL_W * healthRatio);
            healthValue = Math.round(ivy.getHealth());
        }
        int armorValue = getArmorValue();
        int armorFillWidth = getArmorFillWidth();

        int iconX = x + VITALS_DEST_ICON_X;
        int barX = x + VITALS_DEST_BAR_X;
        int healthY = y + VITALS_DEST_HEALTH_Y;
        int armorY = y + VITALS_DEST_ARMOR_Y;

        com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, TEXTURE, iconX, healthY, VITALS_SOURCE_HEART_ICON_X, VITALS_SOURCE_HEART_ICON_Y,
                ICON_SIZE, ICON_SIZE, TEX_W, TEX_H);
        com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, TEXTURE, barX, healthY, VITALS_SOURCE_BAR_X, VITALS_SOURCE_HEALTH_BAR_Y,
                BAR_W, BAR_H, TEX_W, TEX_H);
        if (healthFillWidth > 0) {
            com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, TEXTURE, barX + 3, healthY + 3,
                    VITALS_SOURCE_FILL_X, VITALS_SOURCE_HEALTH_FILL_Y,
                    healthFillWidth, FILL_H, TEX_W, TEX_H);
        }

        com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, TEXTURE, iconX, armorY, VITALS_SOURCE_ARMOR_ICON_X, VITALS_SOURCE_ARMOR_ICON_Y,
                ICON_SIZE, ICON_SIZE, TEX_W, TEX_H);
        com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, TEXTURE, barX, armorY, VITALS_SOURCE_BAR_X, VITALS_SOURCE_ARMOR_BAR_Y,
                BAR_W, BAR_H, TEX_W, TEX_H);
        if (armorFillWidth > 0) {
            com.leon.saintsdragons.client.ui.Gui26.blit(guiGraphics, TEXTURE, barX + 3, armorY + 3,
                    VITALS_SOURCE_FILL_X, VITALS_SOURCE_ARMOR_FILL_Y,
                    armorFillWidth, FILL_H, TEX_W, TEX_H);
        }

        renderVitalValue(guiGraphics, String.valueOf(healthValue), x + VITALS_VALUE_X, healthY);
        renderVitalValue(guiGraphics, String.valueOf(armorValue), x + VITALS_VALUE_X, armorY);
    }

    private void renderVitalValue(GuiGraphicsExtractor guiGraphics, String value, int x, int y) {
        com.leon.saintsdragons.client.ui.Gui26.text(guiGraphics, this.font, value, x - this.font.width(value), y, 0x000000, false);
    }

    private int getArmorFillWidth() {
        return Math.round(FILL_W * getArmorRatio());
    }

    private float getArmorRatio() {
        return Math.max(0.0F, Math.min(1.0F, getArmorValue() / 20.0F));
    }

    private int getArmorValue() {
        int armor = 0;
        armor += getArmorDefense(IvyInventoryMenu.HELMET_SLOT);
        armor += getArmorDefense(IvyInventoryMenu.CHESTPLATE_SLOT);
        armor += getArmorDefense(IvyInventoryMenu.LEGGINGS_SLOT);
        armor += getArmorDefense(IvyInventoryMenu.BOOTS_SLOT);
        return armor;
    }

    private int getArmorDefense(int slotIndex) {
        ItemStack stack = this.menu.getSlot(slotIndex).getItem();
        net.minecraft.world.entity.EquipmentSlot armorSlot = com.leon.saintsdragons.common.item.util.EquipmentStacks.armorSlot(stack);
        return armorSlot == null ? 0 : (int) Math.round(com.leon.saintsdragons.common.item.util.EquipmentStacks.applyModifiers(stack, armorSlot, net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 0.0D));
    }

    private void renderIvyEffects(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        IvyTheDragonMerchant ivy = getIvy();
        List<MobEffectInstance> effects = new ArrayList<>();
        if (ivy != null) {
            effects.addAll(ivy.getActiveEffects());
        }
        if (hasBoundStegonautBinder()) {
            addMissingEffect(effects, MobEffects.RESISTANCE);
            addMissingEffect(effects, MobEffects.ABSORPTION);
        }
        if (effects.isEmpty()) {
            return;
        }

        effects.sort(Comparator.comparing(effect -> effect.getEffect().value().getDescriptionId()));

        int x = this.leftPos - EFFECT_ICON_SIZE - EFFECTS_LEFT_GAP;
        int y = this.topPos + EFFECTS_TOP_Y;
        MobEffectInstance hoveredEffect = null;

        Minecraft minecraft = Minecraft.getInstance();
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance effect = effects.get(i);
            int iconY = y + i * (EFFECT_ICON_SIZE + EFFECT_ICON_GAP);
            guiGraphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                    net.minecraft.client.gui.Hud.getMobEffectSprite(effect.getEffect()), x, iconY, EFFECT_ICON_SIZE, EFFECT_ICON_SIZE);

            if (mouseX >= x && mouseX < x + EFFECT_ICON_SIZE
                    && mouseY >= iconY && mouseY < iconY + EFFECT_ICON_SIZE) {
                hoveredEffect = effect;
            }
        }

        if (hoveredEffect != null) {
            guiGraphics.setComponentTooltipForNextFrame(this.font, getEffectTooltip(hoveredEffect), mouseX, mouseY);
        }
    }

    private List<Component> getEffectTooltip(MobEffectInstance effect) {
        List<Component> lines = new ArrayList<>();
        Component name = effect.getEffect().value().getDisplayName();
        if (effect.getAmplifier() > 0) {
            name = Component.translatable(
                    "potion.withAmplifier",
                    name,
                    Component.translatable("potion.potency." + effect.getAmplifier())
            );
        }
        lines.add(name);
        if (effect.getDuration() > 0) {
            lines.add(MobEffectUtil.formatDuration(effect, 1.0F, minecraft.level != null ? minecraft.level.tickRateManager().tickrate() : 20.0F));
        }
        return lines;
    }

    private void addMissingEffect(List<MobEffectInstance> effects, net.minecraft.core.Holder<MobEffect> mobEffect) {
        for (MobEffectInstance effect : effects) {
            if (effect.getEffect().equals(mobEffect)) {
                return;
            }
        }
        effects.add(new MobEffectInstance(mobEffect, 0, 0, false, false, false));
    }

    private boolean hasBoundStegonautBinder() {
        for (int slot = 0; slot < IvyInventoryMenu.IVY_SLOT_COUNT; slot++) {
            ItemStack stack = this.menu.getSlot(slot).getItem();
            if (!stack.isEmpty()
                    && stack.getItem() instanceof StegonautBinderItem
                    && StegonautBinderItem.isBound(stack)
                    && StegonautBinderItem.getBoundStegonautUUID(stack) != null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private IvyTheDragonMerchant getIvy() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        Entity entity = minecraft.level.getEntity(this.menu.getIvyEntityId());
        return entity instanceof IvyTheDragonMerchant ivy ? ivy : null;
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        renderIvyEffects(guiGraphics, mouseX, mouseY);
        this.extractTooltip(guiGraphics, mouseX, mouseY);
    }
}
