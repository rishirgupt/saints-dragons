package com.leon.saintsdragons.client.render26;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * GeckoLib 5 models only see the render state, not the animatable. This model resolves its model and
 * texture from the animatable while the render state is being built and carries them over as data tickets.
 */
public class ResolvedGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    private static final DataTicket<Identifier> MODEL = DataTicket.create("saintsdragons_model", Identifier.class);
    private static final DataTicket<Identifier> TEXTURE = DataTicket.create("saintsdragons_texture", Identifier.class);

    private final Function<T, Identifier> model;
    private final Function<T, Identifier> texture;
    private final Function<T, Identifier> animation;
    private final Identifier fallbackModel;
    private final Identifier fallbackTexture;

    public ResolvedGeoModel(Function<T, Identifier> model, Function<T, Identifier> texture,
                            Function<T, Identifier> animation, Identifier fallbackModel, Identifier fallbackTexture) {
        this.model = model;
        this.texture = texture;
        this.animation = animation;
        this.fallbackModel = fallbackModel;
        this.fallbackTexture = fallbackTexture;
    }

    /** Fixed resources, given in the 1.20.1 layout (geo/..., textures/..., animations/...). */
    public static <T extends GeoAnimatable> ResolvedGeoModel<T> fixed(String legacyModel, String texture,
                                                                     String legacyAnimation) {
        Identifier modelId = geo(SaintsDragonsCommon.rl(legacyModel));
        Identifier textureId = SaintsDragonsCommon.rl(texture);
        Identifier animationId = animation(SaintsDragonsCommon.rl(legacyAnimation));
        return new ResolvedGeoModel<>(a -> modelId, a -> textureId, a -> animationId, modelId, textureId);
    }

    /** As {@link #fixed(String, String, String)} but with the texture taken from another namespace. */
    public static <T extends GeoAnimatable> ResolvedGeoModel<T> fixed(String legacyModel, String texture,
                                                                     String legacyAnimation, String textureNamespace) {
        Identifier modelId = geo(SaintsDragonsCommon.rl(legacyModel));
        Identifier textureId = Identifier.fromNamespaceAndPath(textureNamespace, texture);
        Identifier animationId = animation(SaintsDragonsCommon.rl(legacyAnimation));
        return new ResolvedGeoModel<>(a -> modelId, a -> textureId, a -> animationId, modelId, textureId);
    }

    /** Converts a 1.20.1 model path (geo/x/y.geo.json) to the GeckoLib 5 id (x/y under geckolib/models). */
    public static Identifier geo(Identifier legacy) {
        return legacy.withPath(strip(legacy.getPath(), "geo/", ".geo.json"));
    }

    /** Converts a 1.20.1 animation path (animations/x/y.animation.json) to the GeckoLib 5 id. */
    public static Identifier animation(Identifier legacy) {
        return legacy.withPath(strip(legacy.getPath(), "animations/", ".animation.json"));
    }

    private static String strip(String path, String prefix, String suffix) {
        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }
        if (path.endsWith(suffix)) {
            path = path.substring(0, path.length() - suffix.length());
        }
        return path;
    }

    @Override
    public void addAdditionalStateData(T animatable, @Nullable Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(MODEL, model.apply(animatable));
        renderState.addGeckolibData(TEXTURE, texture.apply(animatable));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(MODEL, fallbackModel);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, fallbackTexture);
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return animation.apply(animatable);
    }
}
