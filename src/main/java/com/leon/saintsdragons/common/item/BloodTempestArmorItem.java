package com.leon.saintsdragons.common.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.leon.saintsdragons.common.config.ToolsArmorConfig;
import com.leon.saintsdragons.common.registry.ModArmorMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

public class BloodTempestArmorItem extends ModArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BloodTempestArmorItem(ModArmorMaterials armorMaterial, ArmorType type, Properties properties) {
        // Armor values come from the config and are baked into the attribute component (applies after restart).
        super(armorMaterial, type, properties, ConfiguredItemAttributes.armor(type, configuredDefense(type),
                ToolsArmorConfig.BLOOD_TEMPEST_TOUGHNESS.get(),
                ToolsArmorConfig.BLOOD_TEMPEST_KNOCKBACK_RESISTANCE.get()).build());
    }

    private static double configuredDefense(ArmorType type) {
        return switch (type) {
            case HELMET -> ToolsArmorConfig.BLOOD_TEMPEST_HELMET_ARMOR.get();
            case CHESTPLATE, BODY -> ToolsArmorConfig.BLOOD_TEMPEST_CHESTPLATE_ARMOR.get();
            case LEGGINGS -> ToolsArmorConfig.BLOOD_TEMPEST_LEGGINGS_ARMOR.get();
            case BOOTS -> ToolsArmorConfig.BLOOD_TEMPEST_BOOTS_ARMOR.get();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.empty());
        tooltip.accept(Component.empty()
                .append(Component.translatable("item.saintsdragons.blood_tempest_armor.tooltip.title")
                        .withStyle(ChatFormatting.DARK_RED))
                .append(Component.literal(" "))
                .append(Component.translatable("item.saintsdragons.blood_tempest_armor.tooltip.full_set")
                        .withStyle(ChatFormatting.GRAY)));
        tooltip.accept(Component.translatable("item.saintsdragons.blood_tempest_armor.tooltip.description")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }


    @Override
    public void createGeoRenderer(java.util.function.Consumer<com.geckolib.animatable.client.GeoRenderProvider> consumer) {
        consumer.accept(com.leon.saintsdragons.client.renderer.armor.ItemRenderProviders.bloodTempestArmor());
    }
}
