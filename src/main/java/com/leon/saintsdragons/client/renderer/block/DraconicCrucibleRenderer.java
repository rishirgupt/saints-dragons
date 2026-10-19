package com.leon.saintsdragons.client.renderer.block;

import com.leon.saintsdragons.client.model.block.DraconicCrucibleEntity;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.block.DraconicCrucibleBlock;
import com.leon.saintsdragons.common.block.DraconicCrucibleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DraconicCrucibleRenderer
        implements BlockEntityRenderer<DraconicCrucibleBlockEntity, DraconicCrucibleRenderer.State> {
    private static final Identifier INACTIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/block/draconic_crucible.png");
    private static final Identifier ACTIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/block/draconic_crucible_active.png");
    private final DraconicCrucibleEntity model;

    public static class State extends BlockEntityRenderState {
        public boolean active;
        public boolean open;
        public long animationTimeMillis;
        public Direction facing = Direction.NORTH;
    }

    public DraconicCrucibleRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new DraconicCrucibleEntity(context.bakeLayer(DraconicCrucibleEntity.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DraconicCrucibleBlockEntity crucible, State state, float partialTick,
                                   Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(crucible, state, partialTick, cameraPosition, breakProgress);
        state.active = crucible.getBlockState().getValue(DraconicCrucibleBlock.LIT);
        state.open = crucible.hasAnimationState() ? crucible.isOpen() : !state.active;
        state.animationTimeMillis = crucible.hasAnimationState()
                ? crucible.getAnimationTimeMillis(partialTick)
                : 1_000L;
        state.facing = crucible.getBlockState().getValue(DraconicCrucibleBlock.FACING);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
        poseStack.scale(1.0F, -1.0F, -1.0F);

        Identifier texture = state.active ? ACTIVE_TEXTURE : INACTIVE_TEXTURE;
        submitNodeCollector.submitModel(this.model,
                new DraconicCrucibleEntity.State(state.open, state.animationTimeMillis), poseStack,
                RenderTypes.entityCutoutCull(texture), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, 0,
                state.breakProgress);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
