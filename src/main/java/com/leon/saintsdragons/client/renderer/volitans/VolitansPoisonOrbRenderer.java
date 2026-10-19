package com.leon.saintsdragons.client.renderer.volitans;

import com.leon.saintsdragons.client.model.volitans.VolitansPoisonOrbModel;
import com.leon.saintsdragons.server.entity.effect.volitans.VolitansPoisonOrbEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import com.leon.saintsdragons.client.render26.LivingGeoRenderer;
import com.leon.saintsdragons.client.render26.ObjectGeoRenderer;

public class VolitansPoisonOrbRenderer extends ObjectGeoRenderer<VolitansPoisonOrbEntity> {
    public VolitansPoisonOrbRenderer(EntityRendererProvider.Context context) {
        super(context, new VolitansPoisonOrbModel());
        shadowRadius = 0;
    }
    @Override
    public void render(VolitansPoisonOrbEntity entity, float yaw, float partialTick,
                       PoseStack poses, MultiBufferSource buffers, int light) {
        super.render(entity, yaw, partialTick, poses, buffers, LightCoordsUtil.FULL_BRIGHT);
    }
    @Override
    protected void applyRotations(VolitansPoisonOrbEntity entity, PoseStack poses,
                                  float age, float yaw, float partialTick) {
        float scale = entity.getVisualScale() * 0.9F;
        poses.scale(scale, scale, scale);
        poses.translate(0, -0.25, 0);
    }
    @Override
    protected int modelLight(VolitansPoisonOrbEntity entity, int packedLight) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    public RenderType getRenderType(VolitansPoisonOrbEntity animatable, Identifier texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityCutoutCull(texture);
    }
}
