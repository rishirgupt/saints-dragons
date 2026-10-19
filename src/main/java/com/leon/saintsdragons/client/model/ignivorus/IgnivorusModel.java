package com.leon.saintsdragons.client.model.ignivorus;

import com.leon.saintsdragons.client.model.DragonGeoModel;
import com.leon.saintsdragons.client.model.DragonModelPoseHelper;
import com.leon.saintsdragons.client.ui.DraconicCodexScreen;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.dragons.ignivorus.Ignivorus;
import com.leon.saintsdragons.server.entity.part.IgnivorusPoseOffsets;
import net.minecraft.resources.Identifier;
import com.leon.saintsdragons.client.render26.PoseState;
import com.leon.saintsdragons.client.render26.EntityModelData;

public class IgnivorusModel extends DragonGeoModel<Ignivorus> {
    public IgnivorusModel() {
        super("ignivorus");
    }

    private static final Identifier CRIMSON_TEXTURE = SaintsDragonsCommon.rl("textures/entity/ignivorus/crimson_ignivorus.png");
    private static final Identifier CRIMSON_FEMALE_TEXTURE = SaintsDragonsCommon.rl("textures/entity/ignivorus/crimson_ignivorus_female.png");

    @Override
    protected Identifier getAdultTexture(Ignivorus entity) {
        if (entity.hasCustomTextureVariant()) {
            return super.getAdultTexture(entity);
        }
        if (entity.getTextureVariant() == Ignivorus.VARIANT_CRIMSON) {
            return entity.isFemale() ? CRIMSON_FEMALE_TEXTURE : CRIMSON_TEXTURE;
        }
        return super.getAdultTexture(entity);
    }

    @Override
    public void setCustomAnimations(Ignivorus entity, long instanceId, PoseState<Ignivorus> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (DraconicCodexScreen.RENDERING_IN_GUI.get()) {
            return;
        }
        if (entity.isScentAssessing()) {
            return;
        }
        EntityModelData modelData = animationState.modelData();
        if (modelData == null) return;

        float partialTick = animationState.getPartialTick();
        if (entity.getSkyfallElapsedTicks(partialTick) >= 0.0F) {
            return;
        }

        if (entity.isAlive()) {
            if (entity.isDeadOrDying()){
                return;
            }
            IgnivorusPoseOffsets.apply(entity, partialTick,
                    modelData.headPitch(), modelData.netHeadYaw(), DragonModelPoseHelper.poseEditor(this));
        }
    }

}
