package com.leon.saintsdragons.common.item.tools;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

/** Worldroot tool material. Repair items come from the item tag saintsdragons:repairs/worldroot. */
public enum WorldrootTier {
    INSTANCE(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1950, 8.8F, 3.8F, 14, "worldroot");

    private final ToolMaterial material;

    WorldrootTier(TagKey<Block> incorrectBlocksForDrops, int uses, float speed, float attackDamageBonus,
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
