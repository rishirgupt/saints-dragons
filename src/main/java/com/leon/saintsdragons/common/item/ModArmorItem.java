package com.leon.saintsdragons.common.item;

import com.leon.saintsdragons.common.registry.ModArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.Nullable;

/** Base for the mod's armor pieces. Vanilla no longer has an ArmorItem class; armor is an Item with armor properties. */
public class ModArmorItem extends Item {
    private final ModArmorMaterials armorMaterial;
    private final ArmorType type;

    public ModArmorItem(ModArmorMaterials armorMaterial, ArmorType type, Properties properties) {
        this(armorMaterial, type, properties, null);
    }

    /** @param attributes replaces the material's default armor attributes when not null */
    protected ModArmorItem(ModArmorMaterials armorMaterial, ArmorType type, Properties properties,
                           @Nullable ItemAttributeModifiers attributes) {
        super(attributes == null
                ? properties.humanoidArmor(armorMaterial.material(), type)
                : properties.humanoidArmor(armorMaterial.material(), type).attributes(attributes));
        this.armorMaterial = armorMaterial;
        this.type = type;
    }

    public ArmorType getType() {
        return type;
    }

    public ModArmorMaterials getArmorMaterial() {
        return armorMaterial;
    }
}
