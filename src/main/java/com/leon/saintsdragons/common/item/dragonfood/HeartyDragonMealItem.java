package com.leon.saintsdragons.common.item.dragonfood;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class HeartyDragonMealItem extends Item {
    public HeartyDragonMealItem(Properties props) {
        super(props);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        return InteractionResult.FAIL;
    }
}