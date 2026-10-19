package com.leon.saintsdragons.client.renderer.volitans;

import com.leon.saintsdragons.client.model.volitans.ArrowOfVenomModel;
import com.leon.saintsdragons.server.entity.effect.volitans.ArrowOfVenomEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import com.leon.saintsdragons.client.render26.LivingGeoRenderer;
import com.leon.saintsdragons.client.render26.ObjectGeoRenderer;

public class ArrowOfVenomRenderer extends ObjectGeoRenderer<ArrowOfVenomEntity> {
    public ArrowOfVenomRenderer(EntityRendererProvider.Context context) {
        super(context, new ArrowOfVenomModel());
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(@NotNull ArrowOfVenomEntity entity, float entityYaw, float partialTick,
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
