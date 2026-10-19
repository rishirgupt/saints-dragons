package com.leon.saintsdragons.client.model.cindervane;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.client.renderer.vfx.BeamRenderTypes;
import com.leon.saintsdragons.server.entity.effect.cindervane.CindervaneFireballEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class CindervaneFireballModel extends PosedGeoModel<CindervaneFireballEntity> {
    private static final Identifier MODEL = SaintsDragonsCommon.rl("objects/shared/fireball/fireball_stage_1");
    private static final Identifier TEXTURE = SaintsDragonsCommon.rl("textures/objects/shared/fireball/fireball_stage_1.png");
    private static final Identifier ANIMATION = SaintsDragonsCommon.rl("objects/shared/fireball/fireball_stage_1");

    @Override
    public Identifier getModelResource(CindervaneFireballEntity entity) { return MODEL; }

    @Override
    public Identifier getTextureResource(CindervaneFireballEntity entity) { return TEXTURE; }

    @Override
    public Identifier getAnimationResource(CindervaneFireballEntity entity) { return ANIMATION; }

    public RenderType getRenderType(CindervaneFireballEntity entity, Identifier texture) {
        return BeamRenderTypes.translucent(texture);
    }
}
