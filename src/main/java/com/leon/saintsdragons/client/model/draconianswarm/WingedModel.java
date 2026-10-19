package com.leon.saintsdragons.client.model.draconianswarm;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.draconianswarm.Winged;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import com.leon.saintsdragons.client.render26.PoseState;
import com.leon.saintsdragons.client.render26.PosedGeoModel;
import com.leon.saintsdragons.client.render26.EntityModelData;

public class WingedModel extends PosedGeoModel<Winged> {
    private static final Identifier MODEL = SaintsDragonsCommon.rl("entity/draconian_swarm/winged/winged");
    private static final Identifier TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/draconian_swarm/winged/winged.png");
    private static final Identifier ANIMATIONS =
            SaintsDragonsCommon.rl("entity/draconian_swarm/winged/winged");

    @Override
    public Identifier getModelResource(Winged animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(Winged animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(Winged animatable) {
        return ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(Winged entity, long instanceId, PoseState<Winged> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);
        if (!entity.isAlive()) {
            return;
        }

        float pitch = Mth.clamp(entity.getFlightPitchRadians(animationState.getPartialTick()), -0.95F, 0.95F);
        getBone("root").ifPresent(bone -> bone.setRotX(bone.getInitialSnapshot().getRotX() + pitch));

        EntityModelData modelData = animationState.modelData();
        if (modelData != null) {
            float lookPitch = Mth.clamp(modelData.headPitch() * Mth.DEG_TO_RAD, -0.55F, 0.55F);
            float lookYaw = Mth.clamp(modelData.netHeadYaw() * Mth.DEG_TO_RAD, -0.75F, 0.75F);
            getBone("head").ifPresent(bone -> {
                bone.setRotX(bone.getRotX() + lookPitch);
                bone.setRotY(bone.getRotY() + lookYaw);
            });
        }
    }
}
