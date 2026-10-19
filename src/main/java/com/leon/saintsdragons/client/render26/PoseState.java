package com.leon.saintsdragons.client.render26;

import com.geckolib.animatable.GeoAnimatable;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * What the procedural posing code needs to know about the current render pass.
 *
 * @param entity the entity being drawn, or for armour the entity wearing it; null when there is none
 */
public record PoseState<T extends GeoAnimatable>(T animatable, float partialTick, EntityModelData modelData,
                                                @Nullable Entity entity) {
    public PoseState(T animatable, float partialTick, EntityModelData modelData) {
        this(animatable, partialTick, modelData, animatable instanceof Entity e ? e : null);
    }

    public float getPartialTick() {
        return partialTick;
    }

    public T getAnimatable() {
        return animatable;
    }
}
