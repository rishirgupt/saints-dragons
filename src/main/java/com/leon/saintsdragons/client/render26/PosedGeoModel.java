package com.leon.saintsdragons.client.render26;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Base for the mod's models on GeckoLib 5. Subclasses keep the 1.20.1 shape: they choose model and texture
 * from the animatable and pose bones in {@link #setCustomAnimations}. The choices are resolved while the
 * render state is built; the posing runs as a bone updater in the render pass.
 */
public abstract class PosedGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    private @Nullable BoneSnapshots activeSnapshots;
    private @Nullable BakedGeoModel activeModel;

    private final @Nullable Identifier defaultId;

    protected PosedGeoModel() {
        this.defaultId = null;
    }

    /** Conventional entity assets: entity/<path> for model and animation, textures/entity/<path>.png. */
    protected PosedGeoModel(Identifier assetId) {
        this.defaultId = assetId;
    }

    private Identifier defaulted(String prefix, String suffix) {
        if (defaultId == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " must override its resource getters");
        }
        return defaultId.withPath(prefix + defaultId.getPath() + suffix);
    }

    /** Model id under geckolib/models, without prefix or extension. Must accept null (fallback model). */
    public Identifier getModelResource(T animatable) {
        return defaulted("entity/", "");
    }

    /** Must accept null (fallback texture). */
    public Identifier getTextureResource(T animatable) {
        return defaulted("textures/entity/", ".png");
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return defaulted("entity/", "");
    }

    /** Procedural posing applied on top of the keyframe animations. */
    public void setCustomAnimations(T animatable, long instanceId, PoseState<T> animationState) {
    }

    @Override
    public void addAdditionalStateData(T animatable, @Nullable Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(SdTickets.MODEL, getModelResource(animatable));
        renderState.addGeckolibData(SdTickets.TEXTURE, getTextureResource(animatable));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        Identifier model = renderState.getGeckolibData(SdTickets.MODEL);
        return model != null ? model : getModelResource((T) null);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        Identifier texture = renderState.getGeckolibData(SdTickets.TEXTURE);
        return texture != null ? texture : getTextureResource((T) null);
    }

    /** Runs the posing code against the bone snapshots of this render pass. */
    public final void pose(T animatable, long instanceId, PoseState<T> state, BakedGeoModel model,
                           BoneSnapshots snapshots) {
        BoneSnapshots previousSnapshots = this.activeSnapshots;
        BakedGeoModel previousModel = this.activeModel;
        this.activeSnapshots = snapshots;
        this.activeModel = model;
        try {
            setCustomAnimations(animatable, instanceId, state);
        } finally {
            this.activeSnapshots = previousSnapshots;
            this.activeModel = previousModel;
        }
    }

    /** A bone of the model being posed. Only valid inside {@link #setCustomAnimations}. */
    public Optional<PoseBone> getBone(String name) {
        BoneSnapshots snapshots = this.activeSnapshots;
        BakedGeoModel model = this.activeModel;
        if (snapshots == null || model == null) {
            return Optional.empty();
        }
        return model.getBone(name).flatMap(bone -> snapshots.get(name)
                .map(snapshot -> new PoseBone(bone, snapshot, parent -> getBone(parent).orElse(null))));
    }

    /** Converts a 1.20.1 model path (geo/x/y.geo.json) to the GeckoLib 5 id (x/y). */
    public static Identifier geo(Identifier legacy) {
        return ResolvedGeoModel.geo(legacy);
    }

    /** Converts a 1.20.1 animation path (animations/x/y.animation.json) to the GeckoLib 5 id. */
    public static Identifier animation(Identifier legacy) {
        return ResolvedGeoModel.animation(legacy);
    }
}
