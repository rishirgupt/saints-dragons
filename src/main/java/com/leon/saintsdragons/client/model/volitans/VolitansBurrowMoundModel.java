package com.leon.saintsdragons.client.model.volitans;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.effect.volitans.VolitansBurrowMoundEntity;
import net.minecraft.resources.Identifier;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class VolitansBurrowMoundModel extends PosedGeoModel<VolitansBurrowMoundEntity> {
    private static final Identifier MODEL = SaintsDragonsCommon.rl("objects/volitans/burrow_mound");
    private static final Identifier TEXTURE = SaintsDragonsCommon.rl("textures/objects/volitans/burrow_mound.png");
    private static final Identifier ANIMATION = SaintsDragonsCommon.rl("objects/volitans/burrow_mound");

    @Override
    public Identifier getModelResource(VolitansBurrowMoundEntity animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(VolitansBurrowMoundEntity animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(VolitansBurrowMoundEntity animatable) {
        return ANIMATION;
    }
}
