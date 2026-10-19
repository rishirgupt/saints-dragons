package com.leon.saintsdragons.common.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/**
 * A potion item that always carries one specific potion. The potion lives in the stack's
 * potion_contents component, so vanilla handles drinking, effects and the tooltip.
 */
public class FixedPotionItem extends PotionItem {
    private final Supplier<Potion> potion;

    public FixedPotionItem(Item.Properties properties, Supplier<Potion> potion) {
        super(properties);
        this.potion = potion;
    }

    private ItemStack ensurePotion(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || contents.potion().isEmpty()) {
            // Potions register after items, so the holder is resolved lazily rather than at construction.
            stack.set(DataComponents.POTION_CONTENTS,
                    new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(this.potion.get())));
        }
        return stack;
    }

    @Override
    public ItemStack getDefaultInstance() {
        return ensurePotion(super.getDefaultInstance());
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ensurePotion(player.getItemInHand(hand));
        return super.use(level, player, hand);
    }
}
