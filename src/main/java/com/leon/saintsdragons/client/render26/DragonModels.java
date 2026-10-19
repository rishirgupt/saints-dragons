package com.leon.saintsdragons.client.render26;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.leon.saintsdragons.server.entity.dragons.cindervane.Cindervane;
import com.leon.saintsdragons.server.entity.dragons.ignivorus.Ignivorus;
import com.leon.saintsdragons.server.entity.dragons.raevyx.Raevyx;
import com.leon.saintsdragons.server.entity.dragons.varasuchus.Varasuchus;
import com.leon.saintsdragons.server.entity.dragons.volitans.Volitans;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Model, texture and animation selection for dragons: baby/adult models, male/female textures,
 * data-driven custom variants and each species' built-in rare variant.
 */
public final class DragonModels {
    private static Identifier texture(String path) {
        return SaintsDragonsCommon.rl("textures/entity/" + path + ".png");
    }

    /** A dragon whose assets follow the conventional entity/&lt;name&gt;/&lt;name&gt; layout. */
    public static <T extends DragonEntity> ResolvedGeoModel<T> conventional(String name) {
        return conventional(name, true, null);
    }

    /**
     * @param rareVariant picks the species' built-in rare adult texture base name (e.g. "raevyx_night_gold"),
     *                    or null when the dragon uses the normal texture
     */
    public static <T extends DragonEntity> ResolvedGeoModel<T> conventional(
            String name, boolean hasBabyResources, @Nullable Function<T, String> rareVariant) {
        String base = name + "/" + name;
        String babyBase = hasBabyResources ? name + "/baby_" + name : base;
        Identifier model = SaintsDragonsCommon.rl("entity/" + base);
        Identifier babyModel = SaintsDragonsCommon.rl("entity/" + babyBase);
        Identifier male = texture(base);
        Identifier female = texture(base + "_female");
        Identifier babyMale = texture(babyBase);
        Identifier babyFemale = texture(babyBase + "_female");
        return new ResolvedGeoModel<>(
                dragon -> dragon.isBaby() ? babyModel : model,
                dragon -> {
                    if (dragon.isBaby()) {
                        return dragon.isFemale() ? babyFemale : babyMale;
                    }
                    String rare = rareVariant == null ? null : rareVariant.apply(dragon);
                    if (rare != null) {
                        return texture(name + "/" + rare + (dragon.isFemale() ? "_female" : ""));
                    }
                    if (dragon.hasCustomTextureVariant()) {
                        return dragon.getCustomAdultTextureResource(dragon.isFemale());
                    }
                    return dragon.isFemale() ? female : male;
                },
                dragon -> dragon.isBaby() ? babyModel : model,
                model, male);
    }

    public static ResolvedGeoModel<Cindervane> cindervane() {
        return conventional("cindervane", true, dragon -> {
            if (dragon.isPiebaldVariant()) {
                return "cindervane_piebald";
            }
            return !dragon.hasCustomTextureVariant() && dragon.getTextureVariant() == Cindervane.VARIANT_ALBINO
                    ? "cindervane_albino" : null;
        });
    }

    public static ResolvedGeoModel<Ignivorus> ignivorus() {
        return conventional("ignivorus", true, dragon ->
                !dragon.hasCustomTextureVariant() && dragon.getTextureVariant() == Ignivorus.VARIANT_CRIMSON
                        ? "crimson_ignivorus" : null);
    }

    public static ResolvedGeoModel<Raevyx> raevyx() {
        return conventional("raevyx", true, dragon ->
                !dragon.hasCustomTextureVariant() && dragon.getTextureVariant() == Raevyx.VARIANT_NIGHT_GOLD
                        ? "raevyx_night_gold" : null);
    }

    public static ResolvedGeoModel<Varasuchus> varasuchus() {
        return conventional("varasuchus", true, dragon ->
                Varasuchus.VOID_KISSED_VARIANT_ID.equals(dragon.getTextureVariantId()) ? "varasuchus_void_kissed" : null);
    }

    public static ResolvedGeoModel<Volitans> volitans() {
        return conventional("volitans", true, dragon ->
                !dragon.hasCustomTextureVariant() && dragon.getTextureVariant() == Volitans.VARIANT_BLOODSHOT
                        ? "volitans_bloodshot" : null);
    }

    private DragonModels() {
    }
}
