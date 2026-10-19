package com.leon.saintsdragons.common.item.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;

/**
 * Equipment queries for the component-based item model: armor is any stack with an
 * {@code equippable} component for an armor slot, and item stats live in {@code attribute_modifiers}.
 */
public final class EquipmentStacks {
    /** The humanoid armor slot this stack equips to, or null when it is not armor. */
    @Nullable
    public static EquipmentSlot armorSlot(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) {
            return null;
        }
        EquipmentSlot slot = equippable.slot();
        return slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR ? slot : null;
    }

    public static boolean isArmor(ItemStack stack) {
        return armorSlot(stack) != null;
    }

    public static boolean isArmorFor(ItemStack stack, EquipmentSlot slot) {
        return armorSlot(stack) == slot;
    }

    /** Whether the stack grants any modifier for the attribute while held or worn in the slot. */
    public static boolean hasModifier(ItemStack stack, EquipmentSlot slot, Holder<Attribute> attribute) {
        for (ItemAttributeModifiers.Entry entry : modifiers(stack).modifiers()) {
            if (entry.slot().test(slot) && entry.attribute().equals(attribute)) {
                return true;
            }
        }
        return false;
    }

    /** Applies the stack's modifiers for the attribute in the slot on top of {@code base}. */
    public static double applyModifiers(ItemStack stack, EquipmentSlot slot, Holder<Attribute> attribute, double base) {
        double added = base;
        double multipliedBase = 0.0D;
        double multipliedTotal = 1.0D;
        for (ItemAttributeModifiers.Entry entry : modifiers(stack).modifiers()) {
            if (!entry.slot().test(slot) || !entry.attribute().equals(attribute)) {
                continue;
            }
            AttributeModifier modifier = entry.modifier();
            switch (modifier.operation()) {
                case ADD_VALUE -> added += modifier.amount();
                case ADD_MULTIPLIED_BASE -> multipliedBase += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> multipliedTotal *= 1.0D + modifier.amount();
            }
        }
        return (added + added * multipliedBase) * multipliedTotal;
    }

    public static int enchantmentLevel(HolderLookup.Provider registries, ResourceKey<Enchantment> enchantment,
                                       ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return registries.lookup(Registries.ENCHANTMENT)
                .flatMap(lookup -> lookup.get(enchantment))
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }

    /** The four humanoid armor stacks an entity wears (Inventory no longer exposes an armor list). */
    public static java.util.List<ItemStack> armorItems(net.minecraft.world.entity.LivingEntity entity) {
        return java.util.List.of(
                entity.getItemBySlot(EquipmentSlot.HEAD), entity.getItemBySlot(EquipmentSlot.CHEST),
                entity.getItemBySlot(EquipmentSlot.LEGS), entity.getItemBySlot(EquipmentSlot.FEET));
    }

    /** Saves a container the way SimpleContainer.createTag did: a list of its non-empty stacks. */
    public static net.minecraft.nbt.ListTag saveContainer(net.minecraft.world.Container container, HolderLookup.Provider registries) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                list.add(StackData.saveStack(stack, registries));
            }
        }
        return list;
    }

    /** Counterpart of {@link #saveContainer}: clears the container and adds each saved stack. */
    public static void loadContainer(net.minecraft.world.SimpleContainer container, net.minecraft.nbt.ListTag list, HolderLookup.Provider registries) {
        container.clearContent();
        for (int index = 0; index < list.size(); index++) {
            ItemStack stack = StackData.loadStack(list.getCompoundOrEmpty(index), registries);
            if (!stack.isEmpty()) {
                container.addItem(stack);
            }
        }
    }

    private static ItemAttributeModifiers modifiers(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
    }

    private EquipmentStacks() {
    }
}
