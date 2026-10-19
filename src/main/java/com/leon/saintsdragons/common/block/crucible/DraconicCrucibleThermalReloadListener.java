package com.leon.saintsdragons.common.block.crucible;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class DraconicCrucibleThermalReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Identifier CONFIG_ID = SaintsDragonsCommon.rl("thermal");
    private static final DraconicCrucibleThermalReloadListener INSTANCE =
            new DraconicCrucibleThermalReloadListener();

    private static volatile DraconicCrucibleThermalData current = DraconicCrucibleThermalData.DEFAULT;

    private DraconicCrucibleThermalReloadListener() {
        super(net.minecraft.util.ExtraCodecs.JSON, net.minecraft.resources.FileToIdConverter.json("draconic_crucible"));
    }

    public static DraconicCrucibleThermalReloadListener getInstance() {
        return INSTANCE;
    }

    public static DraconicCrucibleThermalData current() {
        return current;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsonMap,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        JsonElement element = jsonMap.get(CONFIG_ID);
        if (element == null) {
            current = DraconicCrucibleThermalData.DEFAULT;
            SaintsDragonsCommon.LOGGER.warn(
                    "Missing data/saintsdragons/draconic_crucible/thermal.json; using default Crucible thermal values");
            return;
        }

        try {
            JsonObject root = GsonHelper.convertToJsonObject(element, CONFIG_ID.toString());
            current = DraconicCrucibleThermalData.fromJson(root);
            SaintsDragonsCommon.LOGGER.info("Loaded Draconic Crucible thermal data");
        } catch (Exception exception) {
            current = DraconicCrucibleThermalData.DEFAULT;
            SaintsDragonsCommon.LOGGER.error(
                    "Invalid Draconic Crucible thermal data; using defaults", exception);
        }
    }
}
