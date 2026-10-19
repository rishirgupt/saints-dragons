package com.leon.saintsdragons.common.codex;

import com.leon.saintsdragons.common.registry.ModTags;
import com.leon.saintsdragons.common.registry.ModSounds;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class DragonCodexRegistry {
    private static final Map<Identifier, Definition> ENTRIES = new ConcurrentHashMap<>();

    static {
        register(Identifier.fromNamespaceAndPath("saintsdragons", "ignivorus"), new Definition(
                Identifier.fromNamespaceAndPath("saintsdragons", "ecology/ignivorus.txt"),
                "saintsdragons.gui.draconic_codex.ecology.ignivorus.page1",
                ModTags.Items.IGNIVORUS_FOODS, List.of(
                    Identifier.fromNamespaceAndPath("saintsdragons", "ignivorus_scale"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "ignivorus_tooth"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "ignivorus_heart"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "ignivorus_egg"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "ignivorus_wing_hide")
                ),
                new Care(true, true, true), () -> ModSounds.IGNIVORUS_GRUMBLE_1.get()));
        register(Identifier.fromNamespaceAndPath("saintsdragons", "atroxiia"), new Definition(
                Identifier.fromNamespaceAndPath("saintsdragons", "ecology/atroxiia.txt"),
                "saintsdragons.gui.draconic_codex.ecology.atroxiia.page1",
                ModTags.Items.ATROXIIA_FOODS, List.of(
                    Identifier.fromNamespaceAndPath("saintsdragons", "atroxiia_scale"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "atroxiia_egg")
                ),
                new Care(true, true, true), () -> ModSounds.ATROXIIA_GRUMBLE_1.get()));
        register(Identifier.fromNamespaceAndPath("saintsdragons", "raevyx"), new Definition(
                Identifier.fromNamespaceAndPath("saintsdragons", "ecology/raevyx.txt"),
                "saintsdragons.gui.draconic_codex.ecology.raevyx.page1",
                ModTags.Items.RAEVYX_FOODS, List.of(
                    Identifier.fromNamespaceAndPath("saintsdragons", "raevyx_scale"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "raevyx_egg"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "raevyx_wing_hide"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "raevyx_wingtalon")
                ),
                new Care(true, true, true), () -> ModSounds.RAEVYX_GRUMBLE_1.get()));
        register(Identifier.fromNamespaceAndPath("saintsdragons", "varasuchus"), new Definition(
                Identifier.fromNamespaceAndPath("saintsdragons", "ecology/varasuchus.txt"),
                "saintsdragons.gui.draconic_codex.ecology.varasuchus.page1",
                ModTags.Items.VARASUCHUS_FOODS, List.of(
                    Identifier.fromNamespaceAndPath("saintsdragons", "varasuchus_scale"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "varasuchus_egg")
                ),
                new Care(true, true, true), () -> ModSounds.VARASUCHUS_GRUMBLE_1.get()));
        register(Identifier.fromNamespaceAndPath("saintsdragons", "cindervane"), new Definition(
                Identifier.fromNamespaceAndPath("saintsdragons", "ecology/cindervane.txt"),
                "saintsdragons.gui.draconic_codex.ecology.cindervane.page1",
                ModTags.Items.CINDERVANE_FOODS, List.of(
                    Identifier.fromNamespaceAndPath("saintsdragons", "cindervane_scale"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "cindervane_egg")
                ),
                new Care(true, true, true), () -> ModSounds.CINDERVANE_GRUMBLE_1.get()));
        register(Identifier.fromNamespaceAndPath("saintsdragons", "stegonaut"), new Definition(
                Identifier.fromNamespaceAndPath("saintsdragons", "ecology/stegonaut.txt"),
                "saintsdragons.gui.draconic_codex.ecology.stegonaut.page1",
                ModTags.Items.STEGONAUT_FOODS, List.of(
                    Identifier.fromNamespaceAndPath("saintsdragons", "stegonaut_scale"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "stegonaut_egg")
                ),
                new Care(true, true, true), () -> ModSounds.STEGONAUT_GRUMBLE_1.get()));
        register(Identifier.fromNamespaceAndPath("saintsdragons", "volitans"), new Definition(
                Identifier.fromNamespaceAndPath("saintsdragons", "ecology/volitans.txt"),
                "saintsdragons.gui.draconic_codex.ecology.volitans.page1",
                ModTags.Items.VOLITANS_FOODS, List.of(
                    Identifier.fromNamespaceAndPath("saintsdragons", "volitans_scale"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "volitans_spine"),
                    Identifier.fromNamespaceAndPath("saintsdragons", "volitans_egg"),
                    Identifier.fromNamespaceAndPath("minecraft", "salmon"),
                    Identifier.fromNamespaceAndPath("minecraft", "cod"),
                    Identifier.fromNamespaceAndPath("minecraft", "tropical_fish"),
                    Identifier.fromNamespaceAndPath("minecraft", "pufferfish")
                ),
                new Care(true, true, true), () -> ModSounds.VOLITANS_GRUMBLE_3.get()));
        register(Identifier.fromNamespaceAndPath("saintsdragons", "nulljaw"), new Definition(
                Identifier.fromNamespaceAndPath("saintsdragons", "ecology/nulljaw.txt"),
                "saintsdragons.gui.draconic_codex.ecology.nulljaw.page1",
                null, List.of(),
                new Care(true, true, false), () -> ModSounds.NULLJAW_GRUMBLE_1.get()));
    }

    private DragonCodexRegistry() {
    }

    public static void register(Identifier entityTypeId, Definition definition) {
        Objects.requireNonNull(entityTypeId, "entityTypeId");
        Objects.requireNonNull(definition, "definition");
        if (ENTRIES.putIfAbsent(entityTypeId, definition) != null) {
            throw new IllegalArgumentException("Duplicate Codex species: " + entityTypeId);
        }
    }

    public static Identifier speciesId(String savedType) {
        Identifier id = savedType == null ? null : Identifier.tryParse(
                savedType.contains(":") ? savedType : "saintsdragons:" + savedType);
        return id == null ? Identifier.fromNamespaceAndPath("saintsdragons", "unknown") : id;
    }

    public static String speciesKey(DragonEntity dragon) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(dragon.getType());
        return id.getNamespace().equals("saintsdragons") ? id.getPath() : id.toString();
    }

    @Nullable
    public static Definition get(String savedType) {
        return ENTRIES.get(speciesId(savedType));
    }

    public record Care(boolean hunger, boolean happiness, boolean brushing) {
        public static final Care NONE = new Care(false, false, false);
    }

    public record Definition(Identifier ecologyText, String ecologyTranslationKey,
                             @Nullable TagKey<Item> favoriteFoods, List<Identifier> drops,
                             Care care, @Nullable Supplier<SoundEvent> selectionSound) {
        public Definition {
            Objects.requireNonNull(ecologyText, "ecologyText");
            Objects.requireNonNull(ecologyTranslationKey, "ecologyTranslationKey");
            drops = List.copyOf(drops);
            Objects.requireNonNull(care, "care");
        }
    }
}
