package com.leon.saintsdragons.client.renderer.block;

import com.leon.saintsdragons.client.model.block.DraconianNucleusModel;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.block.DraconianNucleusBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DraconianNucleusRenderer
        implements BlockEntityRenderer<DraconianNucleusBlockEntity, DraconianNucleusRenderer.State> {
    private static final Identifier TEXTURE =
            SaintsDragonsCommon.rl("textures/block/draconic_nucleus.png");
    private final DraconianNucleusModel model;

    public static class State extends BlockEntityRenderState {
        public boolean spawning;
        public long animationTimeMillis;
    }

    public DraconianNucleusRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new DraconianNucleusModel(context.bakeLayer(DraconianNucleusModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DraconianNucleusBlockEntity nucleus, State state, float partialTick,
                                   Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(nucleus, state, partialTick, cameraPosition, breakProgress);
        state.spawning = nucleus.hasActiveEncounter();
        state.animationTimeMillis = nucleus.getAnimationTimeMillis(partialTick);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        submitNodeCollector.submitModel(this.model,
                new DraconianNucleusModel.State(state.spawning, state.animationTimeMillis), poseStack,
                RenderTypes.entityTranslucent(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, 0,
                state.breakProgress);
        poseStack.popPose();
    }
}
