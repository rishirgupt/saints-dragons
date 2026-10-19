package com.leon.saintsdragons.client.renderer.volitans;

import com.geckolib.cache.model.GeoBone;
import com.leon.saintsdragons.client.model.volitans.VolitansGroundChunkModel;
import com.leon.saintsdragons.client.render26.BlockCubes;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import com.leon.saintsdragons.client.render26.ObjectGeoRenderer;
import com.leon.saintsdragons.server.entity.effect.volitans.VolitansGroundChunkEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/** An animated chunk of ground: every cube of the model is drawn as the block the chunk was torn from. */
public class VolitansGroundChunkRenderer extends ObjectGeoRenderer<VolitansGroundChunkEntity> {
    private static final Identifier DIRT_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/block/dirt.png");
    private static final float MODEL_FORWARD_YAW_OFFSET = 0.0F;
    private int passLight;

    public VolitansGroundChunkRenderer(EntityRendererProvider.Context context) {
        super(context, new VolitansGroundChunkModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    protected float getDeathMaxRotation(@NotNull VolitansGroundChunkEntity entity) {
        return 0.0F;
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull VolitansGroundChunkEntity entity) {
        return DIRT_TEXTURE;
    }

    @Override
    public void render(@NotNull VolitansGroundChunkEntity entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        passLight = packedLight;
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getVisualYaw() + MODEL_FORWARD_YAW_OFFSET));
    }

    @Override
    protected boolean shouldSubmitModel(VolitansGroundChunkEntity entity, float partialTick) {
        // The cubes themselves are never drawn; blocks take their place.
        return false;
    }

    @Override
    protected boolean wantsBoneTransforms(VolitansGroundChunkEntity entity) {
        return entity.isReady();
    }

    @Override
    protected void onBonePose(VolitansGroundChunkEntity entity, GeoBone bone, PoseStack poseStack,
                              MultiBufferSource bufferSource) {
        BlockCubes.render(bone, entity.getBlockState(), poseStack, bufferSource, passLight,
                OverlayTexture.NO_OVERLAY, 1.0F);
    }
}
