package com.leon.saintsdragons.client.model.ignivorus;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.effect.ignivorus.IgnivorusMagmaPillarEntity;
import net.minecraft.resources.Identifier;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class IgnivorusMagmaPillarModel extends PosedGeoModel<IgnivorusMagmaPillarEntity> {
    private static final Identifier MODEL = SaintsDragonsCommon.rl("objects/ignivorus/ignivorus_magma_pillar");
    private static final Identifier TEXTURE = SaintsDragonsCommon.rl("textures/objects/ignivorus/ignivorus_magma_pillar.png");
    private static final Identifier ANIMATION = SaintsDragonsCommon.rl("objects/ignivorus/ignivorus_magma_pillar");

    @Override
    public Identifier getModelResource(IgnivorusMagmaPillarEntity animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(IgnivorusMagmaPillarEntity animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(IgnivorusMagmaPillarEntity animatable) {
        return ANIMATION;
    }
}
