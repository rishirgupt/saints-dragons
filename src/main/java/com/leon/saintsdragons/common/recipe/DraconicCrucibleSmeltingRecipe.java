package com.leon.saintsdragons.common.recipe;

import com.leon.saintsdragons.common.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Single-input smelting recipe for the Draconic Crucible.
 * JSON: ingredient, result, required_heat_level, processing_time, priority.
 */
public final class DraconicCrucibleSmeltingRecipe implements Recipe<CrucibleRecipeInput> {
    private static final MapCodec<DraconicCrucibleSmeltingRecipe> UNCHECKED_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(DraconicCrucibleSmeltingRecipe::ingredient),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                    Codec.INT.optionalFieldOf("required_heat_level", 1)
                            .forGetter(DraconicCrucibleSmeltingRecipe::requiredHeatLevel),
                    Codec.INT.optionalFieldOf("processing_time", 200)
                            .forGetter(DraconicCrucibleSmeltingRecipe::processingTime),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(DraconicCrucibleSmeltingRecipe::priority)
            ).apply(instance, DraconicCrucibleSmeltingRecipe::new));

    public static final MapCodec<DraconicCrucibleSmeltingRecipe> CODEC = UNCHECKED_CODEC.validate(recipe -> {
        try {
            validate(recipe.requiredHeatLevel, recipe.processingTime);
            return DataResult.success(recipe);
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, DraconicCrucibleSmeltingRecipe> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private final Ingredient ingredient;
    private final ItemStackTemplate result;
    private final int requiredHeatLevel;
    private final int processingTime;
    private final int priority;
    /** Assigned by the crucible from the recipe's holder; recipes no longer carry their own id. */
    @Nullable
    private Identifier id;

    public DraconicCrucibleSmeltingRecipe(Ingredient ingredient, ItemStackTemplate result, int requiredHeatLevel,
                                          int processingTime, int priority) {
        this.ingredient = ingredient;
        this.result = result;
        this.requiredHeatLevel = requiredHeatLevel;
        this.processingTime = processingTime;
        this.priority = priority;
    }

    static void validate(int requiredHeatLevel, int processingTime) {
        if (requiredHeatLevel < 1 || requiredHeatLevel > 3) {
            throw new IllegalArgumentException("required_heat_level must be between 1 and 3");
        }
        if (processingTime <= 0) {
            throw new IllegalArgumentException("processing_time must be greater than zero");
        }
    }

    public Ingredient ingredient() {
        return this.ingredient;
    }

    public ItemStack result() {
        return this.result.create();
    }

    public int requiredHeatLevel() {
        return this.requiredHeatLevel;
    }

    public int processingTime() {
        return this.processingTime;
    }

    public int priority() {
        return this.priority;
    }

    public Identifier getId() {
        return this.id != null ? this.id : Identifier.fromNamespaceAndPath("saintsdragons", "unknown");
    }

    public DraconicCrucibleSmeltingRecipe bindId(Identifier id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean matches(@NotNull CrucibleRecipeInput input, @NotNull Level level) {
        return input.size() > 0 && this.ingredient.test(input.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CrucibleRecipeInput input) {
        return result();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NotNull String group() {
        return "";
    }

    /** Made in the crucible only: keeps the recipe out of the crafting book, which cannot place it. */
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.FURNACE_MISC;
    }

    @Override
    public @NotNull RecipeSerializer<DraconicCrucibleSmeltingRecipe> getSerializer() {
        return ModRecipes.DRACONIC_CRUCIBLE_SMELTING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<DraconicCrucibleSmeltingRecipe> getType() {
        return ModRecipes.DRACONIC_CRUCIBLE_SMELTING_TYPE.get();
    }
}
