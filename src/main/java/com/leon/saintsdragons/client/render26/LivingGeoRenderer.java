package com.leon.saintsdragons.client.render26;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** GeckoLib renderer for living entities other than the rideable dragons. */
public class LivingGeoRenderer<T extends LivingEntity & GeoAnimatable> extends ModGeoEntityRenderer<T, SdLivingState> {
    public LivingGeoRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    public SdLivingState createRenderState(T animatable, @Nullable Void relatedObject) {
        return new SdLivingState();
    }
}
