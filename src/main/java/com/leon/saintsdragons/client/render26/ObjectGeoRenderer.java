package com.leon.saintsdragons.client.render26;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/** GeckoLib renderer for non-living entities: projectiles, pillars and other spawned objects. */
public class ObjectGeoRenderer<T extends Entity & GeoAnimatable> extends ModGeoEntityRenderer<T, SdEntityState> {
    public ObjectGeoRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    public SdEntityState createRenderState(T animatable, @Nullable Void relatedObject) {
        return new SdEntityState();
    }
}
