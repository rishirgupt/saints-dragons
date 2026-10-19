package com.leon.saintsdragons.client.model.volitans;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.effect.volitans.ArrowOfVenomEntity;
import net.minecraft.resources.Identifier;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class ArrowOfVenomModel extends PosedGeoModel<ArrowOfVenomEntity> {
    private static final Identifier MODEL =
            SaintsDragonsCommon.rl("objects/volitans/arrow_of_venom");
    private static final Identifier TEXTURE =
            SaintsDragonsCommon.rl("textures/objects/volitans/arrow_of_venom.png");
    private static final Identifier ANIMATION =
            SaintsDragonsCommon.rl("objects/volitans/arrow_of_venom");

    @Override
    public Identifier getModelResource(ArrowOfVenomEntity animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(ArrowOfVenomEntity animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(ArrowOfVenomEntity animatable) {
        return ANIMATION;
    }
}
