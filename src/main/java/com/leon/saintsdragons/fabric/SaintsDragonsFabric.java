package com.leon.saintsdragons.fabric;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.init.CommonModEvents;
import com.leon.saintsdragons.common.registry.ModPotionItems;
import com.leon.saintsdragons.common.registry.ModPotions;
import com.leon.saintsdragons.fabric.entity.part.FabricPartEntities;
import com.leon.saintsdragons.fabric.loot.FabricLootTableModifier;
import com.leon.saintsdragons.fabric.mixin.RangedAttributeAccessor;
import com.leon.saintsdragons.fabric.server.FabricServerEvents;
import com.leon.saintsdragons.fabric.world.Spawns;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;

import java.util.List;

public final class SaintsDragonsFabric implements ModInitializer {
    private static final double ATTRIBUTE_CAP = 100000.0D;
    private static final List<ResourceKey<CreativeModeTab>> POTION_HIDE_TABS = List.of(
            CreativeModeTabs.FOOD_AND_DRINKS,
            CreativeModeTabs.INGREDIENTS,
            CreativeModeTabs.COMBAT,
            CreativeModeTabs.TOOLS_AND_UTILITIES,
            CreativeModeTabs.SEARCH
    );
    @Override
    public void onInitialize() {
        SaintsDragonsCommon.init();
        raiseVanillaAttributeCaps();
        FabricPartEntities.register();
        ResourceLoader data = ResourceLoader.get(PackType.SERVER_DATA);
        data.registerReloadListener(SaintsDragonsCommon.rl("dragon_attribute_loader"), com.leon.saintsdragons.common.config.dragon.DragonAttributeConfigLoader.getInstance());
        data.registerReloadListener(SaintsDragonsCommon.rl("dragon_variants"), com.leon.saintsdragons.server.entity.variant.DragonVariantReloadListener.getInstance());
        data.registerReloadListener(SaintsDragonsCommon.rl("ivy_chatter"), com.leon.saintsdragons.server.entity.npc.chatter.IvyChatterReloadListener.getInstance());
        data.registerReloadListener(SaintsDragonsCommon.rl("ivy_trades"), com.leon.saintsdragons.server.entity.npc.trade.IvyTradeReloadListener.getInstance());
        data.registerReloadListener(SaintsDragonsCommon.rl("dialogues"), com.leon.saintsdragons.server.entity.npc.dialogue.DialogueReloadListener.getInstance());
        data.registerReloadListener(SaintsDragonsCommon.rl("dragon_chest_loot"), com.leon.saintsdragons.server.loot.DragonChestLootReloadListener.getInstance());
        data.registerReloadListener(SaintsDragonsCommon.rl("draconic_crucible_thermal"), com.leon.saintsdragons.common.block.crucible.DraconicCrucibleThermalReloadListener.getInstance());
        net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD,
                    net.minecraft.world.item.crafting.Ingredient.of(com.leon.saintsdragons.common.registry.ModItems.VARASUCHUS_SCALE.get()),
                    net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(ModPotions.TIDEGUARD.get()));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD,
                    net.minecraft.world.item.crafting.Ingredient.of(com.leon.saintsdragons.common.registry.ModItems.IGNIVORUS_TOOTH.get()),
                    net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(ModPotions.SEARING.get()));
        });
        FabricServerEvents.init();
        FabricLootTableModifier.register();
        CommonModEvents.registerEntityAttributes(SaintsDragonsFabric::registerDefaultAttributes);
        CommonModEvents.registerSpawnPlacements(SpawnPlacements::register);
        Spawns.register();
        CommonModEvents.registerCreativeTabEntries((tab, itemSupplier) -> CreativeModeTabEvents.modifyOutputEvent(tab).register(entries -> entries.accept(itemSupplier.get())));
        for (ResourceKey<CreativeModeTab> tab : POTION_HIDE_TABS) {
            CreativeModeTabEvents.modifyOutputEvent(tab).register(entries -> {
                entries.getDisplayStacks().removeIf(SaintsDragonsFabric::isHiddenVanillaPotionVariant);
                entries.getSearchTabStacks().removeIf(SaintsDragonsFabric::isHiddenVanillaPotionVariant);
            });
        }
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CommonModEvents.registerCommands(dispatcher));
    }
    private static void raiseVanillaAttributeCaps() {
        raiseAttributeCap(Attributes.MAX_HEALTH, "MAX_HEALTH");
        raiseAttributeCap(Attributes.ARMOR, "ARMOR");
    }
    private static void raiseAttributeCap(net.minecraft.core.Holder<Attribute> attribute, String name) {
        if (!(attribute.value() instanceof RangedAttribute ranged)) {
            return;
        }
        RangedAttributeAccessor accessor = (RangedAttributeAccessor) ranged;
        if (accessor.saintsdragons$getMaxValue() >= ATTRIBUTE_CAP) {
            return;
        }
        accessor.saintsdragons$setMaxValue(ATTRIBUTE_CAP);
        SaintsDragonsCommon.LOGGER.info("Raised {} attribute cap to {}", name, ATTRIBUTE_CAP);
    }

    private static <T extends LivingEntity> void registerDefaultAttributes(
            EntityType<? extends T> type,
            AttributeSupplier.Builder builder
    ) {
        FabricDefaultAttributeRegistry.register(type, builder.build());
    }
    private static boolean isHiddenVanillaPotionVariant(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (stack.is(ModPotionItems.POTION_OF_TIDEGUARD.get()) || stack.is(ModPotionItems.POTION_OF_SEARING.get())) {
            return true;
        }

        if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION) && !stack.is(Items.LINGERING_POTION)) {
            return false;
        }

        net.minecraft.world.item.alchemy.PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        Potion potion = contents == null ? null : contents.potion().map(net.minecraft.core.Holder::value).orElse(null);
        return potion == ModPotions.TIDEGUARD.get() || potion == ModPotions.SEARING.get();
    }
}
