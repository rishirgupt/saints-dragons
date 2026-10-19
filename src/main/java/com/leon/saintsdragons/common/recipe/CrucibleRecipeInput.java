package com.leon.saintsdragons.common.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Recipe input view over the crucible's grid container. */
public record CrucibleRecipeInput(Container container) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return container.getItem(index);
    }

    @Override
    public int size() {
        return container.getContainerSize();
    }
}
