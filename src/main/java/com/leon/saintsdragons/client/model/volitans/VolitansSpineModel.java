package com.leon.saintsdragons.client.model.volitans;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.effect.volitans.VolitansSpineEntity;
import net.minecraft.resources.Identifier;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class VolitansSpineModel extends PosedGeoModel<VolitansSpineEntity> {
    private static final Identifier MODEL =
            SaintsDragonsCommon.rl("objects/volitans/volitans_spine");
    private static final Identifier TEXTURE =
            SaintsDragonsCommon.rl("textures/objects/volitans/volitans_spine.png");
    private static final Identifier ANIMATION =
            SaintsDragonsCommon.rl("objects/volitans/volitans_spine");

    @Override
    public Identifier getModelResource(VolitansSpineEntity animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(VolitansSpineEntity animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(VolitansSpineEntity animatable) {
        return ANIMATION;
    }
}