package com.leon.saintsdragons.server.menu;

import com.leon.saintsdragons.common.registry.ModMenus;
import com.leon.saintsdragons.server.entity.npc.IvyTheDragonMerchant;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class IvyInventoryMenu extends AbstractContainerMenu {
    public static final int HELMET_SLOT = 0;
    public static final int CHESTPLATE_SLOT = 1;
    public static final int LEGGINGS_SLOT = 2;
    public static final int BOOTS_SLOT = 3;
    public static final int SWORD_SLOT = 4;
    public static final int STORAGE_START = 5;
    public static final int STORAGE_COLUMNS = 9;
    public static final int STORAGE_ROWS = 3;
    public static final int STORAGE_COUNT = STORAGE_COLUMNS * STORAGE_ROWS;
    public static final int IVY_SLOT_COUNT = STORAGE_START + STORAGE_COUNT;
    private static final Identifier EMPTY_SWORD_SLOT = Identifier.withDefaultNamespace("container/slot/sword");
    private static final int PLAYER_INV_START = IVY_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container ivyInventory;
    private final ContainerData data;
    @Nullable
    private final IvyTheDragonMerchant ivy;

    public IvyInventoryMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, new SimpleContainer(IVY_SLOT_COUNT), new SimpleContainerData(1));
    }

    public IvyInventoryMenu(int containerId, Inventory playerInventory, IvyTheDragonMerchant ivy) {
        this(containerId, playerInventory, ivy, ivy.getIvyInventory(), new SimpleContainerData(1));
        this.data.set(0, ivy.getId());
    }

    private IvyInventoryMenu(int containerId,
                             Inventory playerInventory,
                             @Nullable IvyTheDragonMerchant ivy,
                             Container ivyInventory,
                             ContainerData data) {
        super(ModMenus.IVY_INVENTORY.get(), containerId);
        this.ivy = ivy;
        this.ivyInventory = ivyInventory;
        this.data = data;
        this.ivyInventory.startOpen(playerInventory.player);
        addDataSlots(data);

        this.addSlot(new ArmorSlot(this.ivyInventory, HELMET_SLOT, 8, 7, EquipmentSlot.HEAD));
        this.addSlot(new ArmorSlot(this.ivyInventory, CHESTPLATE_SLOT, 8, 25, EquipmentSlot.CHEST));
        this.addSlot(new ArmorSlot(this.ivyInventory, LEGGINGS_SLOT, 8, 43, EquipmentSlot.LEGS));
        this.addSlot(new ArmorSlot(this.ivyInventory, BOOTS_SLOT, 8, 61, EquipmentSlot.FEET));
        this.addSlot(new SwordSlot(this.ivyInventory, SWORD_SLOT, 77, 61));

        for (int row = 0; row < STORAGE_ROWS; row++) {
            for (int col = 0; col < STORAGE_COLUMNS; col++) {
                int slotIndex = STORAGE_START + col + row * STORAGE_COLUMNS;
                this.addSlot(new Slot(this.ivyInventory, slotIndex, 8 + col * 18, 83 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 152 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 210));
        }
    }

    public int getIvyEntityId() {
        return this.data.get(0);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.ivy == null) {
            return true;
        }
        return this.ivy.isAlive()
                && this.ivy.isTame()
                && this.ivy.isOwnedBy(player)
                && this.ivy.distanceTo(player) < 8.0F;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return original;
        }

        ItemStack stack = slot.getItem();
        original = stack.copy();

        if (index < IVY_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int equipmentSlot = matchingEquipmentSlot(stack);
            if (equipmentSlot >= 0 && !this.slots.get(equipmentSlot).hasItem()) {
                if (!this.moveItemStackTo(stack, equipmentSlot, equipmentSlot + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, STORAGE_START, IVY_SLOT_COUNT, false)) {
                if (index < PLAYER_INV_END) {
                    if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.ivyInventory.stopOpen(player);
    }

    private static int matchingEquipmentSlot(ItemStack stack) {
        EquipmentSlot armorSlot = com.leon.saintsdragons.common.item.util.EquipmentStacks.armorSlot(stack);
        if (armorSlot != null) {
            return switch (armorSlot) {
                case HEAD -> HELMET_SLOT;
                case CHEST -> CHESTPLATE_SLOT;
                case LEGS -> LEGGINGS_SLOT;
                default -> BOOTS_SLOT;
            };
        }
        if (isWeaponStack(stack)) {
            return SWORD_SLOT;
        }
        return -1;
    }

    private static boolean isWeaponStack(ItemStack stack) {
        return !stack.isEmpty()
                && com.leon.saintsdragons.common.item.util.EquipmentStacks.hasModifier(stack, EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE);
    }

    private static final class ArmorSlot extends Slot {
        private final EquipmentSlot type;
        private final Identifier emptyIcon;

        private ArmorSlot(Container container, int index, int x, int y, EquipmentSlot type) {
            super(container, index, x, y);
            this.type = type;
            this.emptyIcon = switch (type) {
                case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
                case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
                case FEET -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
                default -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
            };
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return com.leon.saintsdragons.common.item.util.EquipmentStacks.isArmorFor(stack, this.type);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public Identifier getNoItemIcon() {
            return this.emptyIcon;
        }
    }

    private static final class SwordSlot extends Slot {
        private SwordSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return isWeaponStack(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public Identifier getNoItemIcon() {
            return EMPTY_SWORD_SLOT;
        }
    }
}
