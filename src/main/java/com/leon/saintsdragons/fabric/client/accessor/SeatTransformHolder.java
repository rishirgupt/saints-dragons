package com.leon.saintsdragons.fabric.client.accessor;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/** Implemented on entity render states: the transform that seats a rider on its dragon for this frame. */
public interface SeatTransformHolder {
    @Nullable Matrix4f saintsdragons$getSeatTransform();

    void saintsdragons$setSeatTransform(@Nullable Matrix4f transform);
}
