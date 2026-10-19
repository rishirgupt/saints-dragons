package com.leon.saintsdragons.client.renderer.stegonaut;

import com.leon.saintsdragons.server.entity.effect.stegonaut.StegonautGroundChunkEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import com.leon.saintsdragons.client.render26.BlockRenderDispatcher;
import com.leon.saintsdragons.client.render26.LegacyEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class StegonautGroundChunkRenderer extends LegacyEntityRenderer<StegonautGroundChunkEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public StegonautGroundChunkRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = new BlockRenderDispatcher(context.getBlockModelResolver());
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(@NotNull StegonautGroundChunkEntity entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        BlockState state = entity.getBlockState();
        if (state.isAir()) {
            return;
        }

        float scale = entity.getVisualScale();
        poseStack.pushPose();
        poseStack.translate(-0.5D * scale, 0.0D, -0.5D * scale);
        poseStack.scale(scale, scale, scale);
        blockRenderer.renderSingleBlock(state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull StegonautGroundChunkEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}