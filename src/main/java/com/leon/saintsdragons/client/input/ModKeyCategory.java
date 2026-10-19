package com.leon.saintsdragons.client.input;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** Key binding category for the mod (categories are registered objects since 1.21.9). */
public final class ModKeyCategory {
    /** Lang key: key.category.saintsdragons.main */
    public static final KeyMapping.Category MAIN =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("saintsdragons", "main"));

    private ModKeyCategory() {
    }
}
