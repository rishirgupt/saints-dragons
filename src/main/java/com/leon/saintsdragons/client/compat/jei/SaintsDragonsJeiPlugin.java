package com.leon.saintsdragons.client.compat.jei;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.recipe.DraconicCrucibleShapedRecipe;
import com.leon.saintsdragons.common.recipe.DraconicCrucibleSmeltingRecipe;
import com.leon.saintsdragons.common.registry.ModItems;
import com.leon.saintsdragons.common.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;

import java.util.List;

@JeiPlugin
public final class SaintsDragonsJeiPlugin implements IModPlugin {
    public static final RecipeType<DraconicCrucibleShapedRecipe> CRUCIBLE_CRAFTING =
            RecipeType.create(SaintsDragonsCommon.MOD_ID, "draconic_crucible",
                    DraconicCrucibleShapedRecipe.class);
    public static final RecipeType<DraconicCrucibleSmeltingRecipe> CRUCIBLE_SMELTING =
            RecipeType.create(SaintsDragonsCommon.MOD_ID, "draconic_crucible_smelting",
                    DraconicCrucibleSmeltingRecipe.class);

    private static final Identifier PLUGIN_ID = SaintsDragonsCommon.rl("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new DraconicCrucibleCraftingJeiCategory(guiHelper, CRUCIBLE_CRAFTING),
                new DraconicCrucibleSmeltingJeiCategory(guiHelper, CRUCIBLE_SMELTING)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<Recipe<?>> recipes = loadBundledRecipes(level.registryAccess());
        SaintsDragonsCommon.LOGGER.info("JEI: {} Saint's Dragons recipes read for the crucible categories", recipes.size());
        registration.addRecipes(CRUCIBLE_CRAFTING, recipes.stream()
                .filter(DraconicCrucibleShapedRecipe.class::isInstance)
                .map(DraconicCrucibleShapedRecipe.class::cast).toList());
        registration.addRecipes(CRUCIBLE_SMELTING, recipes.stream()
                .filter(DraconicCrucibleSmeltingRecipe.class::isInstance)
                .map(DraconicCrucibleSmeltingRecipe.class::cast).toList());
    }

    /**
     * Recipes are no longer sent to the client, so the crucible recipes are read from the mod's own data.
     * Recipes changed or added by a data pack are therefore not reflected here.
     */
    private static List<Recipe<?>> loadBundledRecipes(RegistryAccess registries) {
        List<Recipe<?>> recipes = new ArrayList<>();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        var container = FabricLoader.getInstance().getModContainer(SaintsDragonsCommon.MOD_ID).orElse(null);
        if (container == null) {
            return recipes;
        }
        for (Path root : container.getRootPaths()) {
            Path directory = root.resolve("data").resolve(SaintsDragonsCommon.MOD_ID).resolve("recipe");
            if (!Files.isDirectory(directory)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(directory)) {
                for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".json"))::iterator) {
                    try (Reader reader = Files.newBufferedReader(file)) {
                        Recipe.CODEC.parse(ops, JsonParser.parseReader(reader)).result().ifPresent(recipes::add);
                    } catch (IOException | RuntimeException exception) {
                        SaintsDragonsCommon.LOGGER.warn("Could not read recipe {} for JEI", file, exception);
                    }
                }
            } catch (IOException exception) {
                SaintsDragonsCommon.LOGGER.warn("Could not list recipes for JEI", exception);
            }
        }
        return recipes;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModItems.DRACONIC_CRUCIBLE.get(),
                CRUCIBLE_CRAFTING, CRUCIBLE_SMELTING, RecipeTypes.SMELTING);
    }
}
