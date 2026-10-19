package com.leon.saintsdragons.client.render26;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.Entity;

/**
 * Render layer with the 1.20.1 shape: {@link #render} receives the entity, a pose stack and a buffer source,
 * and may re-render the model with another texture through {@link ModGeoEntityRenderer#reRender}.
 */
public abstract class ModGeoLayer<T extends Entity & GeoAnimatable> extends GeoRenderLayer<T, Void, SdLivingState> {
    private final ModGeoEntityRenderer<T, SdLivingState> owner;

    protected ModGeoLayer(ModGeoEntityRenderer<T, SdLivingState> renderer) {
        super(renderer);
        this.owner = renderer;
    }

    @Override
    public ModGeoEntityRenderer<T, SdLivingState> getRenderer() {
        return owner;
    }

    /** The buffer argument is unused on 26.2 and always null; draw through the buffer source. */
    public abstract void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                                MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                                int packedLight, int packedOverlay);

    @Override
    public void submitRenderTask(RenderPassInfo<SdLivingState> renderPassInfo, SubmitNodeCollector renderTasks) {
        if (!renderPassInfo.willRender()) {
            return;
        }
        SdLivingState state = renderPassInfo.renderState();
        T entity = owner.entityOf(state);
        if (entity == null) {
            return;
        }
        RecordingBufferSource recorder = new RecordingBufferSource();
        owner.beginLayer(renderPassInfo, renderTasks);
        PoseStack poseStack = renderPassInfo.poseStack();
        poseStack.pushPose();
        if (owner.layerPose() != null) {
            poseStack.last().set(owner.layerPose());
        }
        try {
            render(renderPassInfo.poseStack(), entity, renderPassInfo.model(),
                    owner.getRenderType(state, owner.getTextureLocation(state)), recorder, null,
                    state.getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 1.0F),
                    renderPassInfo.packedLight(), renderPassInfo.packedOverlay());
        } finally {
            poseStack.popPose();
            owner.endLayer();
        }
        recorder.flush(renderTasks);
    }
}
