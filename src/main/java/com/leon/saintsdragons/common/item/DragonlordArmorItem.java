package com.leon.saintsdragons.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.leon.saintsdragons.common.config.ToolsArmorConfig;
import com.leon.saintsdragons.common.registry.ModAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.GeoItem;
import com.geckolib.constant.DataTickets;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DragonlordArmorItem extends ModArmorItem implements GeoItem {
    public static final String FLIGHT_CONTROLLER = "dragonlord_flight";
    public static final String FLAP_TRIGGER = "flap";
    public static final com.geckolib.constant.dataticket.DataTicket<Boolean> GLIDING =
            com.geckolib.constant.dataticket.DataTicket.create("saintsdragons_dragonlord_gliding", Boolean.class);
    private static final RawAnimation GLIDE =
            RawAnimation.begin().thenLoop("animation.dragonlord_armor.glide");
    private static final RawAnimation FLAP =
            RawAnimation.begin().thenPlay("animation.dragonlord_armor.flap");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DragonlordArmorItem(com.leon.saintsdragons.common.registry.ModArmorMaterials material, net.minecraft.world.item.equipment.ArmorType type, Properties properties) {
        super(material, type, properties, configuredAttributes(type));
        GeoItem.registerSyncedAnimatable(this);
    }

    /** Dragonlord pieces add max health, fire and blast resistance on top of armor; baked at construction (config applies after restart). */
    private static net.minecraft.world.item.component.ItemAttributeModifiers configuredAttributes(net.minecraft.world.item.equipment.ArmorType type) {
        net.minecraft.world.entity.EquipmentSlotGroup slot = net.minecraft.world.entity.EquipmentSlotGroup.bySlot(type.getSlot());
        String piece = type.getName();
        return ConfiguredItemAttributes.armor(type, configuredDefense(type), ToolsArmorConfig.DRAGONLORD_TOUGHNESS.get(), configuredKnockbackResistance(type))
                .add(Attributes.MAX_HEALTH, new AttributeModifier(
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("saintsdragons", "dragonlord_max_health." + piece),
                        maxHealthMultiplier(type), AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                .add(ModAttributes.FIRE_RESISTANCE.get(), new AttributeModifier(
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("saintsdragons", "dragonlord_fire_resistance." + piece),
                        ToolsArmorConfig.DRAGONLORD_FIRE_RESISTANCE.get(), AttributeModifier.Operation.ADD_VALUE), slot)
                .add(ModAttributes.BLAST_RESISTANCE.get(), new AttributeModifier(
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("saintsdragons", "dragonlord_blast_resistance." + piece),
                        ToolsArmorConfig.DRAGONLORD_BLAST_RESISTANCE.get(), AttributeModifier.Operation.ADD_VALUE), slot)
                .build();
    }

    private static double configuredKnockbackResistance(net.minecraft.world.item.equipment.ArmorType type) {
        return switch (type) {
            case HELMET -> ToolsArmorConfig.DRAGONLORD_HELMET_KNOCKBACK_RESISTANCE.get();
            case CHESTPLATE, BODY -> ToolsArmorConfig.DRAGONLORD_CHESTPLATE_KNOCKBACK_RESISTANCE.get();
            case LEGGINGS -> ToolsArmorConfig.DRAGONLORD_LEGGINGS_KNOCKBACK_RESISTANCE.get();
            case BOOTS -> ToolsArmorConfig.DRAGONLORD_BOOTS_KNOCKBACK_RESISTANCE.get();
        };
    }


    private static double maxHealthMultiplier(net.minecraft.world.item.equipment.ArmorType type) {
        return switch (type) {
            case HELMET -> ToolsArmorConfig.DRAGONLORD_HELMET_MAX_HEALTH_BONUS.get() / 100.0D;
            case CHESTPLATE, BODY -> ToolsArmorConfig.DRAGONLORD_CHESTPLATE_MAX_HEALTH_BONUS.get() / 100.0D;
            case LEGGINGS -> ToolsArmorConfig.DRAGONLORD_LEGGINGS_MAX_HEALTH_BONUS.get() / 100.0D;
            case BOOTS -> ToolsArmorConfig.DRAGONLORD_BOOTS_MAX_HEALTH_BONUS.get() / 100.0D;
        };
    }



    private static double configuredDefense(net.minecraft.world.item.equipment.ArmorType type) {
        return switch (type) {
            case HELMET -> ToolsArmorConfig.DRAGONLORD_HELMET_ARMOR.get();
            case CHESTPLATE, BODY -> ToolsArmorConfig.DRAGONLORD_CHESTPLATE_ARMOR.get();
            case LEGGINGS -> ToolsArmorConfig.DRAGONLORD_LEGGINGS_ARMOR.get();
            case BOOTS -> ToolsArmorConfig.DRAGONLORD_BOOTS_ARMOR.get();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext tooltipContext, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, tooltipContext, tooltipDisplay, tooltip, flag);
        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.double_jump.title")
                .withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.double_jump.description")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.dragonfall.title")
                .withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.dragonfall.description")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.aerial_dominion.title")
                .withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.aerial_dominion.enter")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.aerial_dominion.boost")
                .withStyle(ChatFormatting.GRAY));
    }



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<DragonlordArmorItem> flightController = new com.leon.saintsdragons.util.animation.ReleasingAnimationController<>(
                FLIGHT_CONTROLLER,
                3,
                state -> {
                    // The armor renderer publishes whether the wearer is gliding in the full set.
                    if (!state.getDataOrDefault(GLIDING, false)) {
                        return PlayState.STOP;
                    }

                    state.setAndContinue(GLIDE);
                    return PlayState.CONTINUE;
                }
        );
        flightController.triggerableAnim(FLAP_TRIGGER, FLAP);
        controllers.add(flightController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }


    @Override
    public void createGeoRenderer(java.util.function.Consumer<com.geckolib.animatable.client.GeoRenderProvider> consumer) {
        consumer.accept(com.leon.saintsdragons.client.renderer.armor.ItemRenderProviders.dragonlordArmor());
    }
}
