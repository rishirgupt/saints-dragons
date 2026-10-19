package com.leon.saintsdragons.client.renderer.draconianswarm;

import com.leon.saintsdragons.client.model.draconianswarm.WingedModel;
import com.leon.saintsdragons.server.entity.draconianswarm.Winged;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.LightCoordsUtil;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.leon.saintsdragons.client.render26.LivingGeoRenderer;
import com.leon.saintsdragons.client.render26.ObjectGeoRenderer;

public class WingedRenderer extends LivingGeoRenderer<Winged> {
    public WingedRenderer(EntityRendererProvider.Context context) {
        super(context, new WingedModel());
        this.shadowRadius = 0.35F;
    }

    @Override
    public void render(@NotNull Winged entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, LightCoordsUtil.FULL_BRIGHT);
    }

    @Override
    public RenderType getRenderType(Winged animatable, Identifier texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityCutoutCull(texture);
    }

    @Override
    protected int modelLight(Winged entity, int packedLight) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    protected float getDeathMaxRotation(Winged entity) {
        return 0.0F;
    }
}
