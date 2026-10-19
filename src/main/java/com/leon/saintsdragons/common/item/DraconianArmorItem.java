package com.leon.saintsdragons.common.item;

import com.leon.saintsdragons.common.registry.ModArmorMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

public class DraconianArmorItem extends ModArmorItem {
    public DraconianArmorItem(ModArmorMaterials material, ArmorType type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("item.saintsdragons.draconian_armor.tooltip.passive.title")
                .withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.empty()
                .append(Component.translatable("item.saintsdragons.draconian_armor.tooltip.full_set")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(" "))
                .append(Component.translatable("item.saintsdragons.draconian_armor.tooltip.passive.description")
                        .withStyle(ChatFormatting.GRAY)));
    }
}
