package com.leon.saintsdragons.client.render26;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Entity renderer that keeps the 1.20.1 shape for hand-drawn effect entities: subclasses implement
 * {@link #render} against the entity and a buffer source, and the geometry is replayed into the 26.2
 * submit queue.
 */
public abstract class LegacyEntityRenderer<T extends Entity> extends EntityRenderer<T, LegacyEntityRenderer.State> {
    /** Render state that carries the entity itself; client entities are only read on the render thread. */
    public static class State extends EntityRenderState {
        public @Nullable Entity entity;
        public float partialTick;
        public float yaw;
    }

    protected LegacyEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
    }

    /** Kept for source compatibility; hand-drawn renderers bind their own textures. */
    public @Nullable Identifier getTextureLocation(T entity) {
        return null;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.entity = entity;
        state.partialTick = partialTicks;
        state.yaw = entity.getYRot(partialTicks);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (state.entity != null) {
            RecordingBufferSource recorder = new RecordingBufferSource();
            poseStack.pushPose();
            try {
                render((T) state.entity, state.yaw, state.partialTick, poseStack, recorder, state.lightCoords);
            } finally {
                poseStack.popPose();
            }
            recorder.flush(submitNodeCollector);
        }
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
