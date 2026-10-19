package com.leon.saintsdragons.client.model.mossback;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.dragons.Mossback;
import net.minecraft.resources.Identifier;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class MossbackModel extends PosedGeoModel<Mossback> {
    private static final Identifier ADULT_MODEL = SaintsDragonsCommon.rl("entity/mossback/mossback");
    private static final Identifier BABY_MODEL = SaintsDragonsCommon.rl("entity/mossback/baby_mossback");
    private static final Identifier ADULT_TEXTURE = SaintsDragonsCommon.rl("textures/entity/mossback/mossback.png");
    private static final Identifier BABY_TEXTURE = SaintsDragonsCommon.rl("textures/entity/mossback/baby_mossback.png");
    private static final Identifier ADULT_ANIMATION = SaintsDragonsCommon.rl("entity/mossback/mossback");
    private static final Identifier BABY_ANIMATION = SaintsDragonsCommon.rl("entity/mossback/baby_mossback");

    public MossbackModel() {
        super(SaintsDragonsCommon.rl("mossback"));
    }

    @Override
    public Identifier getModelResource(Mossback entity) {
        return entity != null && entity.isBaby() ? BABY_MODEL : ADULT_MODEL;
    }

    @Override
    public Identifier getTextureResource(Mossback entity) {
        return entity != null && entity.isBaby() ? BABY_TEXTURE : ADULT_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(Mossback entity) {
        return entity != null && entity.isBaby() ? BABY_ANIMATION : ADULT_ANIMATION;
    }
}
