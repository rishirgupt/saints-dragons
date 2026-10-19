package com.leon.saintsdragons.client.model.volitans;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.effect.volitans.VolitansGroundChunkEntity;
import net.minecraft.resources.Identifier;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class VolitansGroundChunkModel extends PosedGeoModel<VolitansGroundChunkEntity> {
    private static final Identifier MODEL = SaintsDragonsCommon.rl("objects/volitans/ground_chunk");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/block/dirt.png");
    private static final Identifier ANIMATION = SaintsDragonsCommon.rl("objects/volitans/ground_chunk");

    @Override
    public Identifier getModelResource(VolitansGroundChunkEntity animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(VolitansGroundChunkEntity animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(VolitansGroundChunkEntity animatable) {
        return ANIMATION;
    }
}
