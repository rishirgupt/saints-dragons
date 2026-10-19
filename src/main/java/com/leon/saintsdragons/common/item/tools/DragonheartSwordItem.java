package com.leon.saintsdragons.common.item.tools;

import com.google.common.collect.Multimap;
import com.leon.saintsdragons.common.config.ToolsArmorConfig;
import com.leon.saintsdragons.common.item.ConfiguredItemAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class DragonheartSwordItem extends Item {
    private static final int DRAGONLORD_FIRE_ASPECT_SECONDS = 8;
    public static final Identifier ENTITY_REACH_MODIFIER_ID = Identifier.fromNamespaceAndPath("saintsdragons", "sword_entity_reach");
    public static final Identifier TARGETING_REACH_MODIFIER_ID = Identifier.fromNamespaceAndPath("saintsdragons", "sword_targeting_reach");
    public static final double VANILLA_ENTITY_REACH = 3.0D;
    public static final double VANILLA_BLOCK_REACH = 4.5D;

    private final double entityReach;
    private final float criticalDamageBonus;
    private final DragonheartWeaponTier tier;

    public DragonheartSwordItem(DragonheartWeaponTier tier,
                                int attackDamageModifier,
                                float attackSpeedModifier,
                                double entityReach,
                                float criticalDamageBonus,
                                Properties properties) {
        super(properties.sword(tier.material(),
                ConfiguredItemAttributes.toolDamageModifier(configuredDamage(tier, attackDamageModifier), tier.getAttackDamageBonus()),
                ConfiguredItemAttributes.toolSpeedModifier(configuredSpeed(tier, attackSpeedModifier)))
                .attributes(ConfiguredItemAttributes.weapon(configuredDamage(tier, attackDamageModifier), configuredSpeed(tier, attackSpeedModifier))
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE,
                                new AttributeModifier(ENTITY_REACH_MODIFIER_ID, configuredReach(tier, entityReach) - VANILLA_ENTITY_REACH, AttributeModifier.Operation.ADD_VALUE),
                                net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE,
                                new AttributeModifier(TARGETING_REACH_MODIFIER_ID, Math.max(0.0D, configuredReach(tier, entityReach) - VANILLA_BLOCK_REACH), AttributeModifier.Operation.ADD_VALUE),
                                net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                        .build()));
        this.entityReach = entityReach;
        this.criticalDamageBonus = criticalDamageBonus;
        this.tier = tier;
    }

    // Attack damage, speed and reach are baked into the attribute component at construction (config applies after restart).
    private static double configuredDamage(DragonheartWeaponTier tier, int fallbackModifier) {
        var value = tier == DragonheartWeaponTier.CHUNK ? ToolsArmorConfig.BLOOD_TEMPEST_KATANA_DAMAGE : ToolsArmorConfig.DRAGONLORD_SWORD_DAMAGE;
        return value == null ? 1.0D + tier.getAttackDamageBonus() + fallbackModifier : value.get();
    }

    private static double configuredReach(DragonheartWeaponTier tier, double fallback) {
        var value = tier == DragonheartWeaponTier.CHUNK ? ToolsArmorConfig.BLOOD_TEMPEST_KATANA_REACH : ToolsArmorConfig.DRAGONLORD_SWORD_REACH;
        return value == null ? fallback : value.get();
    }

    private static double configuredSpeed(DragonheartWeaponTier tier, float fallbackModifier) {
        var value = tier == DragonheartWeaponTier.CHUNK ? ToolsArmorConfig.BLOOD_TEMPEST_KATANA_SPEED : ToolsArmorConfig.DRAGONLORD_SWORD_SPEED;
        return value == null ? 4.0D + fallbackModifier : value.get();
    }

    public double getEntityReach() {
        if (ToolsArmorConfig.BLOOD_TEMPEST_KATANA_REACH == null || ToolsArmorConfig.DRAGONLORD_SWORD_REACH == null) {
            return this.entityReach;
        }
        return isBloodTempest() ? ToolsArmorConfig.BLOOD_TEMPEST_KATANA_REACH.get() : ToolsArmorConfig.DRAGONLORD_SWORD_REACH.get();
    }

    public double getEntityReachBonus() {
        return getEntityReach() - VANILLA_ENTITY_REACH;
    }

    public double getTargetingReachBonus() {
        return getEntityReach() - VANILLA_BLOCK_REACH;
    }

    public float getCriticalDamageBonus() {
        if (ToolsArmorConfig.BLOOD_TEMPEST_KATANA_CRITICAL_BONUS == null
                || ToolsArmorConfig.DRAGONLORD_SWORD_CRITICAL_BONUS == null) {
            return this.criticalDamageBonus;
        }
        return (float) (isBloodTempest()
                ? ToolsArmorConfig.BLOOD_TEMPEST_KATANA_CRITICAL_BONUS.get()
                : ToolsArmorConfig.DRAGONLORD_SWORD_CRITICAL_BONUS.get());
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (!isBloodTempest()) {
            target.igniteForSeconds(DRAGONLORD_FIRE_ASPECT_SECONDS);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext tooltipContext, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, tooltipContext, tooltipDisplay, tooltip, flag);
        if (!isBloodTempest()) {
            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("item.saintsdragons.dragonlord_sword.tooltip.ability.title")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.saintsdragons.dragonlord_sword.tooltip.ability.description")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("item.saintsdragons.dragonlord_sword.tooltip.description")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            return;
        }

        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("item.saintsdragons.blood_tempest_katana.tooltip.passive.title")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.accept(Component.translatable("item.saintsdragons.blood_tempest_katana.tooltip.passive.description")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("item.saintsdragons.blood_tempest_katana.tooltip.ability.title")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.accept(Component.translatable("item.saintsdragons.blood_tempest_katana.tooltip.ability.description")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("item.saintsdragons.blood_tempest_katana.tooltip.quote")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }

    private boolean isBloodTempest() {
        return this.tier == DragonheartWeaponTier.CHUNK;
    }




}
