package com.leon.saintsdragons.client.renderer.stegonaut;

import com.leon.saintsdragons.client.model.stegonaut.StegonautAmethystPillarModel;
import com.leon.saintsdragons.server.entity.effect.stegonaut.StegonautAmethystPillarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import com.leon.saintsdragons.client.render26.LivingGeoRenderer;
import com.leon.saintsdragons.client.render26.ObjectGeoRenderer;

public class StegonautAmethystPillarRenderer extends ObjectGeoRenderer<StegonautAmethystPillarEntity> {
    public StegonautAmethystPillarRenderer(EntityRendererProvider.Context context) {
        super(context, new StegonautAmethystPillarModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    protected float getDeathMaxRotation(@NotNull StegonautAmethystPillarEntity entity) {
        return 0.0F;
    }

    @Override
    public void render(@NotNull StegonautAmethystPillarEntity entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        float scale = entity.getVisualScale();
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        super.render(entity, entity.getYHeadRot(), partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
