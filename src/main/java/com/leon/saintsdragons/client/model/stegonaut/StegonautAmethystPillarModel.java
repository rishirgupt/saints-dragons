package com.leon.saintsdragons.client.model.stegonaut;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.effect.stegonaut.StegonautAmethystPillarEntity;
import net.minecraft.resources.Identifier;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class StegonautAmethystPillarModel extends PosedGeoModel<StegonautAmethystPillarEntity> {
    private static final Identifier MODEL = SaintsDragonsCommon.rl("objects/stegonaut/amethyst_pillar");
    private static final Identifier TEXTURE = SaintsDragonsCommon.rl("textures/objects/stegonaut/amethyst_pillar.png");
    private static final Identifier ANIMATION = SaintsDragonsCommon.rl("objects/stegonaut/amethyst_pillar");

    @Override
    public Identifier getModelResource(StegonautAmethystPillarEntity animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(StegonautAmethystPillarEntity animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(StegonautAmethystPillarEntity animatable) {
        return ANIMATION;
    }
}
