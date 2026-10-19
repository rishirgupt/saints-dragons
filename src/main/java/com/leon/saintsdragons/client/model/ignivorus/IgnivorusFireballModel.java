package com.leon.saintsdragons.client.model.ignivorus;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.client.renderer.vfx.BeamRenderTypes;
import com.leon.saintsdragons.server.entity.effect.ignivorus.IgnivorusFireballEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class IgnivorusFireballModel extends PosedGeoModel<IgnivorusFireballEntity> {
    private static final Identifier MODEL = SaintsDragonsCommon.rl("objects/shared/fireball/fireball_stage_1");
    private static final Identifier TEXTURE = SaintsDragonsCommon.rl("textures/objects/shared/fireball/fireball_stage_1.png");
    private static final Identifier ANIMATION = SaintsDragonsCommon.rl("objects/shared/fireball/fireball_stage_1");

    private static final Identifier STAGE_TWO_MODEL = SaintsDragonsCommon.rl("objects/shared/fireball/fireball_stage_2");
    private static final Identifier STAGE_TWO_TEXTURE = SaintsDragonsCommon.rl("textures/objects/shared/fireball/fireball_stage_2.png");
    private static final Identifier STAGE_TWO_ANIMATION = SaintsDragonsCommon.rl("objects/shared/fireball/fireball_stage_2");

    private static final Identifier STAGE_THREE_MODEL = SaintsDragonsCommon.rl("objects/ignivorus/ignivorus_fireball_stage_3");
    private static final Identifier STAGE_THREE_TEXTURE = SaintsDragonsCommon.rl("textures/objects/ignivorus/ignivorus_fireball_stage_3.png");
    private static final Identifier STAGE_THREE_ANIMATION = SaintsDragonsCommon.rl("objects/ignivorus/ignivorus_fireball_stage_3");

    @Override
    public Identifier getModelResource(IgnivorusFireballEntity entity) {
        if (entity.getVisualScale() >= 8.0F) return STAGE_THREE_MODEL;
        return entity.getVisualScale() >= 6.0F ? STAGE_TWO_MODEL : MODEL;
    }

    @Override
    public Identifier getTextureResource(IgnivorusFireballEntity entity) {
        if (entity.getVisualScale() >= 8.0F) return STAGE_THREE_TEXTURE;
        return entity.getVisualScale() >= 6.0F ? STAGE_TWO_TEXTURE : TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(IgnivorusFireballEntity entity) {
        if (entity.getVisualScale() >= 8.0F) return STAGE_THREE_ANIMATION;
        return entity.getVisualScale() >= 6.0F ? STAGE_TWO_ANIMATION : ANIMATION;
    }

    public RenderType getRenderType(IgnivorusFireballEntity entity, Identifier texture) {
        return BeamRenderTypes.translucent(texture);
    }
}
