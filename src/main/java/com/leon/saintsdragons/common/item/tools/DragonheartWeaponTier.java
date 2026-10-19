package com.leon.saintsdragons.common.item.tools;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

/** Dragonheart weapon materials. Repair items come from the item tags saintsdragons:repairs/dragonheart_*. */
public enum DragonheartWeaponTier {
    CHUNK(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2600, 5.0F, 5.0F, 18, "dragonheart_chunk"),
    ALLOY(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3400, 10.0F, 7.0F, 20, "dragonheart_alloy");

    private final ToolMaterial material;

    DragonheartWeaponTier(TagKey<Block> incorrectBlocksForDrops, int uses, float speed, float attackDamageBonus,
                          int enchantmentValue, String repairTag) {
        this.material = new ToolMaterial(incorrectBlocksForDrops, uses, speed, attackDamageBonus, enchantmentValue,
                TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("saintsdragons", "repairs/" + repairTag)));
    }

    public ToolMaterial material() {
        return material;
    }

    public float getAttackDamageBonus() {
        return material.attackDamageBonus();
    }
}
