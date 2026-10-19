package com.leon.saintsdragons.client.model.volitans;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.effect.volitans.VolitansPoisonOrbEntity;
import net.minecraft.resources.Identifier;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public final class VolitansPoisonOrbModel extends PosedGeoModel<VolitansPoisonOrbEntity> {
    @Override
    public Identifier getModelResource(VolitansPoisonOrbEntity entity) {
        return SaintsDragonsCommon.rl("objects/volitans/volitans_poison_orb");
    }
    @Override
    public Identifier getTextureResource(VolitansPoisonOrbEntity entity) {
        return SaintsDragonsCommon.rl("textures/objects/volitans/volitans_poison_orb.png");
    }
    @Override
    public Identifier getAnimationResource(VolitansPoisonOrbEntity entity) {
        return SaintsDragonsCommon.rl("objects/volitans/volitans_poison_orb");
    }
}
