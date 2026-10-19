package com.leon.saintsdragons.common.recipe;

import com.leon.saintsdragons.common.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shaped recipe for the Draconic Crucible's 3x3 grid.
 * JSON: pattern (1-3 rows of 1-3 chars), key (symbol to ingredient), result, required_heat_level,
 * processing_time, priority.
 */
public final class DraconicCrucibleShapedRecipe implements Recipe<CrucibleRecipeInput> {
    public static final int GRID_SIZE = 9;

    private static final MapCodec<Raw> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("pattern").forGetter(Raw::pattern),
            Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).fieldOf("key").forGetter(Raw::key),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(Raw::result),
            Codec.INT.optionalFieldOf("required_heat_level", 1).forGetter(Raw::requiredHeatLevel),
            Codec.INT.optionalFieldOf("processing_time", 200).forGetter(Raw::processingTime),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(Raw::priority)
    ).apply(instance, Raw::new));

    public static final MapCodec<DraconicCrucibleShapedRecipe> CODEC = RAW_CODEC.flatXmap(raw -> {
        try {
            return DataResult.success(new DraconicCrucibleShapedRecipe(raw));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }, recipe -> DataResult.success(recipe.raw));

    public static final StreamCodec<RegistryFriendlyByteBuf, DraconicCrucibleShapedRecipe> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private final Raw raw;
    private final int width;
    private final int height;
    /** Row-major, width * height entries; empty means the slot must be empty. */
    private final List<Optional<Ingredient>> ingredients;
    /** Assigned by the crucible from the recipe's holder; recipes no longer carry their own id. */
    @Nullable
    private Identifier id;

    private DraconicCrucibleShapedRecipe(Raw raw) {
        List<String> pattern = raw.pattern();
        if (pattern.isEmpty() || pattern.size() > 3) {
            throw new IllegalArgumentException("Draconic Crucible patterns must contain between 1 and 3 rows");
        }
        int patternWidth = -1;
        for (String row : pattern) {
            if (row.isEmpty() || row.length() > 3) {
                throw new IllegalArgumentException("Each Draconic Crucible pattern row must contain between 1 and 3 characters");
            }
            if (patternWidth == -1) {
                patternWidth = row.length();
            } else if (row.length() != patternWidth) {
                throw new IllegalArgumentException("All Draconic Crucible pattern rows must have the same width");
            }
        }
        for (String symbol : raw.key().keySet()) {
            if (symbol.length() != 1 || symbol.charAt(0) == ' ') {
                throw new IllegalArgumentException("Recipe key symbols must be one non-space character");
            }
        }
        DraconicCrucibleSmeltingRecipe.validate(raw.requiredHeatLevel(), raw.processingTime());

        this.raw = raw;
        this.height = pattern.size();
        this.width = patternWidth;
        this.ingredients = new ArrayList<>(this.width * this.height);
        int occupiedSlots = 0;
        for (int row = 0; row < this.height; row++) {
            for (int column = 0; column < this.width; column++) {
                char symbol = pattern.get(row).charAt(column);
                if (symbol == ' ') {
                    this.ingredients.add(Optional.empty());
                    continue;
                }
                Ingredient ingredient = raw.key().get(String.valueOf(symbol));
                if (ingredient == null) {
                    throw new IllegalArgumentException("Pattern references undefined symbol '" + symbol + "'");
                }
                this.ingredients.add(Optional.of(ingredient));
                occupiedSlots++;
            }
        }
        if (occupiedSlots == 0) {
            throw new IllegalArgumentException("Draconic Crucible patterns must contain at least one ingredient");
        }
    }

    @Override
    public boolean matches(@NotNull CrucibleRecipeInput input, @NotNull Level level) {
        return matches(input.container());
    }

    public boolean matches(Container container) {
        return container.getContainerSize() >= GRID_SIZE && findMatch(container) != null;
    }

    public boolean consumeInputs(Container container) {
        Match match = findMatch(container);
        if (match == null) {
            return false;
        }
        for (int row = 0; row < this.height; row++) {
            for (int column = 0; column < this.width; column++) {
                int ingredientColumn = match.mirrored ? this.width - column - 1 : column;
                if (this.ingredients.get(ingredientColumn + row * this.width).isPresent()) {
                    int slot = match.offsetX + column + (match.offsetY + row) * 3;
                    container.removeItem(slot, 1);
                }
            }
        }
        return true;
    }

    /** Crafting remainders (e.g. empty buckets) for each grid slot, before inputs are consumed. */
    public NonNullList<ItemStack> getRemainingItems(Container container) {
        NonNullList<ItemStack> remainders = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < remainders.size(); slot++) {
            ItemStack stack = container.getItem(slot);
            var remainder = stack.isEmpty() ? null : stack.getItem().getCraftingRemainder();
            if (remainder != null) {
                remainders.set(slot, remainder.create());
            }
        }
        return remainders;
    }

    @Nullable
    private Match findMatch(Container container) {
        for (int offsetY = 0; offsetY <= 3 - this.height; offsetY++) {
            for (int offsetX = 0; offsetX <= 3 - this.width; offsetX++) {
                if (matchesAt(container, offsetX, offsetY, false)) {
                    return new Match(offsetX, offsetY, false);
                }
                if (matchesAt(container, offsetX, offsetY, true)) {
                    return new Match(offsetX, offsetY, true);
                }
            }
        }
        return null;
    }

    private boolean matchesAt(Container container, int offsetX, int offsetY, boolean mirrored) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int recipeX = column - offsetX;
                int recipeY = row - offsetY;
                Optional<Ingredient> expected = Optional.empty();
                if (recipeX >= 0 && recipeX < this.width && recipeY >= 0 && recipeY < this.height) {
                    int ingredientX = mirrored ? this.width - recipeX - 1 : recipeX;
                    expected = this.ingredients.get(ingredientX + recipeY * this.width);
                }
                ItemStack actual = container.getItem(column + row * 3);
                if (expected.isEmpty() ? !actual.isEmpty() : !expected.get().test(actual)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int requiredHeatLevel() {
        return this.raw.requiredHeatLevel();
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public ItemStack result() {
        return this.raw.result().create();
    }

    public int processingTime() {
        return this.raw.processingTime();
    }

    public int priority() {
        return this.raw.priority();
    }

    /** Row-major ingredient grid; empty entries are slots that must be empty. */
    public List<Optional<Ingredient>> ingredientGrid() {
        return this.ingredients;
    }

    public Identifier getId() {
        return this.id != null ? this.id : Identifier.fromNamespaceAndPath("saintsdragons", "unknown");
    }

    public DraconicCrucibleShapedRecipe bindId(Identifier id) {
        this.id = id;
        return this;
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
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public @NotNull RecipeSerializer<DraconicCrucibleShapedRecipe> getSerializer() {
        return ModRecipes.DRACONIC_CRUCIBLE_SHAPED_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<DraconicCrucibleShapedRecipe> getType() {
        return ModRecipes.DRACONIC_CRUCIBLE_SHAPED_TYPE.get();
    }

    private record Raw(List<String> pattern, Map<String, Ingredient> key, ItemStackTemplate result,
                       int requiredHeatLevel, int processingTime, int priority) {
    }

    private record Match(int offsetX, int offsetY, boolean mirrored) {
    }
}
