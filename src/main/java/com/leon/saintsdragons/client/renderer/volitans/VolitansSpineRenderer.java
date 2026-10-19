package com.leon.saintsdragons.client.renderer.volitans;

import com.leon.saintsdragons.client.model.volitans.VolitansSpineModel;
import com.leon.saintsdragons.server.entity.effect.volitans.VolitansSpineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import com.leon.saintsdragons.client.render26.LivingGeoRenderer;
import com.leon.saintsdragons.client.render26.ObjectGeoRenderer;

public class VolitansSpineRenderer extends ObjectGeoRenderer<VolitansSpineEntity> {
    public VolitansSpineRenderer(EntityRendererProvider.Context context) {
        super(context, new VolitansSpineModel());
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(@NotNull VolitansSpineEntity entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}