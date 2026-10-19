package com.leon.saintsdragons.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.Nullable;

/** JSON conversion for text components; replaces the removed Component.Serializer helpers. */
public final class ComponentJson {
    public static JsonElement toJsonTree(Component component) {
        return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, component).getOrThrow();
    }

    public static String toJson(Component component) {
        return toJsonTree(component).toString();
    }

    @Nullable
    public static Component fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json).result().orElse(null);
    }

    @Nullable
    public static Component fromJson(@Nullable String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return fromJson(JsonParser.parseString(json));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private ComponentJson() {
    }
}
