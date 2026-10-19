package com.leon.saintsdragons.server.entity.variant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.registry.DragonSpeciesRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DragonVariantReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Gson GSON = new GsonBuilder().create();
    private static final DragonVariantReloadListener INSTANCE = new DragonVariantReloadListener();

    private DragonVariantReloadListener() {
        super(net.minecraft.util.ExtraCodecs.JSON, net.minecraft.resources.FileToIdConverter.json("dragon_variants"));
    }

    public static DragonVariantReloadListener getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsonMap,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        Map<Identifier, List<DragonVariantDefinition>> parsed = new LinkedHashMap<>();
        for (Map.Entry<Identifier, JsonElement> entry : jsonMap.entrySet()) {
            Identifier fileId = entry.getKey();
            try {
                JsonObject root = GsonHelper.convertToJsonObject(entry.getValue(), fileId.toString());
                Identifier dragonId = resolveDragonId(fileId, root);
                JsonArray variants = GsonHelper.getAsJsonArray(root, "variants");
                for (JsonElement element : variants) {
                    DragonVariantDefinition definition = parseVariant(fileId, dragonId, GsonHelper.convertToJsonObject(element, fileId + " variant"));
                    parsed.computeIfAbsent(dragonId, id -> new ArrayList<>()).add(definition);
                }
            } catch (Exception exception) {
                SaintsDragonsCommon.LOGGER.error("Failed to parse dragon variant file {}", fileId, exception);
            }
        }
        SaintsDragonVariantRegistry.replaceDatapackVariants(parsed);
    }

    private static Identifier resolveDragonId(Identifier fileId, JsonObject root) {
        if (root.has("dragon")) {
            return parseVariantId(fileId.getNamespace(), GsonHelper.getAsString(root, "dragon"));
        }
        if (SaintsDragonVariantRegistry.hasDefaults(fileId)
                || DragonSpeciesRegistry.get(fileId) != null) {
            return fileId;
        }
        Identifier legacyId = SaintsDragonsCommon.rl(fileId.getPath());
        return SaintsDragonVariantRegistry.hasDefaults(legacyId) ? legacyId : fileId;
    }

    private static DragonVariantDefinition parseVariant(Identifier fileId,
                                                       Identifier dragonId,
                                                       JsonObject input) {
        String name = GsonHelper.getAsString(input, "name");
        Identifier variantId = input.has("id")
                ? Identifier.parse(GsonHelper.getAsString(input, "id"))
                : parseVariantId(fileId.getNamespace(), name);
        int weight = GsonHelper.getAsInt(input, "weight");
        DragonVariantDefinition.BiomeRestrictions allowedBiomes = parseBiomes(input, "allowed_biomes");
        DragonVariantDefinition.BiomeRestrictions bannedBiomes = parseBiomes(input, "banned_biomes");
        DragonVariantDefinition.AltitudeRestriction altitude = parseAltitude(input);
        return new DragonVariantDefinition(
                variantId,
                dragonId,
                name,
                weight,
                DragonVariantDefinition.NO_LEGACY_ID,
                allowedBiomes,
                bannedBiomes,
                altitude
        );
    }

    private static Identifier parseVariantId(String namespace, String name) {
        if (name.indexOf(':') >= 0) {
            return Identifier.parse(name);
        }
        return Identifier.fromNamespaceAndPath(namespace, name);
    }

    private static DragonVariantDefinition.BiomeRestrictions parseBiomes(JsonObject input, String key) {
        if (!input.has(key)) {
            return null;
        }
        JsonObject object = GsonHelper.getAsJsonObject(input, key);
        List<Identifier> biomes = parseResourceList(object, "biome");
        List<Identifier> tags = parseResourceList(object, "tag");
        return new DragonVariantDefinition.BiomeRestrictions(biomes, tags);
    }

    private static List<Identifier> parseResourceList(JsonObject object, String key) {
        List<Identifier> result = new ArrayList<>();
        if (!object.has(key)) {
            return result;
        }
        JsonArray array = GsonHelper.getAsJsonArray(object, key);
        for (JsonElement element : array) {
            result.add(Identifier.parse(element.getAsString()));
        }
        return result;
    }

    private static DragonVariantDefinition.AltitudeRestriction parseAltitude(JsonObject input) {
        if (!input.has("altitude")) {
            return DragonVariantDefinition.AltitudeRestriction.ANY;
        }
        JsonObject object = GsonHelper.getAsJsonObject(input, "altitude");
        int min = object.has("min") ? GsonHelper.getAsInt(object, "min") : Integer.MIN_VALUE;
        int max = object.has("max") ? GsonHelper.getAsInt(object, "max") : Integer.MAX_VALUE;
        return new DragonVariantDefinition.AltitudeRestriction(min, max);
    }
}
