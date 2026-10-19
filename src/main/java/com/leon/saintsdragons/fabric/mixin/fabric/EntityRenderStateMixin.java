package com.leon.saintsdragons.fabric.mixin.fabric;

import com.leon.saintsdragons.client.renderer.vfx.BloodTempestAfterimageTrail;
import com.leon.saintsdragons.fabric.client.accessor.AfterimageHolder;
import com.leon.saintsdragons.fabric.client.accessor.SeatTransformHolder;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements SeatTransformHolder, AfterimageHolder {
    @Unique
    private @Nullable Matrix4f saintsdragons$seatTransform;

    @Unique
    private List<BloodTempestAfterimageTrail.RenderedSnapshot> saintsdragons$afterimages = List.of();

    @Override
    public @Nullable Matrix4f saintsdragons$getSeatTransform() {
        return this.saintsdragons$seatTransform;
    }

    @Override
    public void saintsdragons$setSeatTransform(@Nullable Matrix4f transform) {
        this.saintsdragons$seatTransform = transform;
    }

    @Override
    public List<BloodTempestAfterimageTrail.RenderedSnapshot> saintsdragons$getAfterimages() {
        return this.saintsdragons$afterimages;
    }

    @Override
    public void saintsdragons$setAfterimages(List<BloodTempestAfterimageTrail.RenderedSnapshot> afterimages) {
        this.saintsdragons$afterimages = afterimages == null ? List.of() : afterimages;
    }
}
