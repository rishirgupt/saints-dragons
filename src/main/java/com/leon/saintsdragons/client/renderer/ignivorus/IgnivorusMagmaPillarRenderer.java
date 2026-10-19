package com.leon.saintsdragons.client.renderer.ignivorus;

import com.leon.saintsdragons.client.model.ignivorus.IgnivorusMagmaPillarModel;
import com.leon.saintsdragons.server.entity.effect.ignivorus.IgnivorusMagmaPillarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import com.leon.saintsdragons.client.render26.LivingGeoRenderer;
import com.leon.saintsdragons.client.render26.ObjectGeoRenderer;

public class IgnivorusMagmaPillarRenderer extends ObjectGeoRenderer<IgnivorusMagmaPillarEntity> {

    public IgnivorusMagmaPillarRenderer(EntityRendererProvider.Context context) {
        super(context, new IgnivorusMagmaPillarModel());
        this.shadowRadius = 1.0F;
    }

    @Override
    protected float getDeathMaxRotation(@NotNull IgnivorusMagmaPillarEntity entity) {
        return 0.0F;
    }

    @Override
    public void render(@NotNull IgnivorusMagmaPillarEntity entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entity.getYHeadRot(), partialTick, poseStack, bufferSource, packedLight);
    }
}