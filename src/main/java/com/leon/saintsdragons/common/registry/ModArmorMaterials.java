package com.leon.saintsdragons.common.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

/** Armor materials. Since 1.21.2 these are plain records; repair items come from item tags under tags/item/repairs. */
public enum ModArmorMaterials {
    DRACONIAN_FLESH("draconian_armor", 25, 3, 7, 5, 3, 18, SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 0.0F),
    DRAGONHEART_CHUNK("dragonheart_chunk", 45, 4, 9, 7, 4, 18, SoundEvents.ARMOR_EQUIP_NETHERITE, 4.0F, 0.15F),
    DRAGONHEART_ALLOY("dragonheart_alloy", 55, 5, 10, 8, 5, 20, SoundEvents.ARMOR_EQUIP_NETHERITE, 5.0F, 0.0F);

    private final String name;
    private final ArmorMaterial material;

    ModArmorMaterials(String name, int durabilityMultiplier, int bootsDefense, int chestplateDefense,
                      int leggingsDefense, int helmetDefense, int enchantmentValue,
                      Holder<SoundEvent> equipSound, float toughness, float knockbackResistance) {
        this.name = name;
        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, bootsDefense);
        defense.put(ArmorType.LEGGINGS, leggingsDefense);
        defense.put(ArmorType.CHESTPLATE, chestplateDefense);
        defense.put(ArmorType.HELMET, helmetDefense);
        defense.put(ArmorType.BODY, chestplateDefense);
        Identifier id = Identifier.fromNamespaceAndPath("saintsdragons", name);
        TagKey<Item> repairItems = TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("saintsdragons", "repairs/" + name));
        this.material = new ArmorMaterial(durabilityMultiplier, defense, enchantmentValue, equipSound,
                toughness, knockbackResistance, repairItems, ResourceKey.create(EquipmentAssets.ROOT_ID, id));
    }

    public ArmorMaterial material() {
        return material;
    }

    public String getName() {
        return "saintsdragons:" + name;
    }
}
