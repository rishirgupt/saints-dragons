package com.leon.saintsdragons.client.model.draconianswarm;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.draconianswarm.Whettled;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import com.leon.saintsdragons.client.render26.PoseState;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class WhettledModel extends PosedGeoModel<Whettled> {
    private static final Identifier MODEL = SaintsDragonsCommon.rl("entity/draconian_swarm/whettled/whettled");
    private static final Identifier TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/draconian_swarm/whettled/whettled.png");
    private static final Identifier ANIMATIONS =
            SaintsDragonsCommon.rl("entity/draconian_swarm/whettled/whettled");

    @Override
    public Identifier getModelResource(Whettled animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(Whettled animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(Whettled animatable) {
        return ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(Whettled entity, long instanceId, PoseState<Whettled> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);
        if (!entity.isAlive()) {
            return;
        }
        float partialTick = animationState.getPartialTick();
        float pitch = Mth.clamp(entity.getFlightPitchRadians(partialTick), -0.95F, 0.95F);
        float drag = Mth.clamp(entity.getTailDragYawRadians(partialTick), -0.95F, 0.95F);
        getBone("root").ifPresent(bone -> bone.setRotX(bone.getInitialSnapshot().getRotX() + pitch));
        applyTailDrag("tail1rot", drag * 0.70F);
        applyTailDrag("tail2rot", drag * 1.00F);
        applyTailDrag("tail3rot", drag * 1.30F);
    }

    private void applyTailDrag(String boneName, float yaw) {
        getBone(boneName).ifPresent(bone -> bone.setRotY(bone.getInitialSnapshot().getRotY() + yaw));
    }
}
