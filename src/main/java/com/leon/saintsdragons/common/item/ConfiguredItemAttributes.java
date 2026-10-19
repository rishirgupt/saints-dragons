package com.leon.saintsdragons.common.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;

/** Builds item attribute components from the configured (final, tooltip-visible) values. */
public final class ConfiguredItemAttributes {
    /** Tool property helpers take the modifier on top of the player's base 1 damage and the material bonus. */
    public static float toolDamageModifier(double attackDamage, float materialBonus) {
        return (float) (attackDamage - 1.0D - materialBonus);
    }

    /** Tool property helpers take the modifier on top of the player's base 4 attack speed. */
    public static float toolSpeedModifier(double attackSpeed) {
        return (float) (attackSpeed - 4.0D);
    }

    public static ItemAttributeModifiers.Builder weapon(double attackDamage, double attackSpeed) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                        attackDamage - 1.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                        attackSpeed - 4.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
    }

    public static ItemAttributeModifiers.Builder armor(ArmorType type, double armor, double toughness,
                                                       double knockbackResistance) {
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
        Identifier id = Identifier.withDefaultNamespace("armor." + type.getName());
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, armor, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(id, toughness, AttributeModifier.Operation.ADD_VALUE), slot);
        if (knockbackResistance != 0.0D) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(id, knockbackResistance, AttributeModifier.Operation.ADD_VALUE), slot);
        }
        return builder;
    }

    private ConfiguredItemAttributes() {
    }
}
