package com.leon.saintsdragons.client.renderer.layer.cindervane;

import com.leon.saintsdragons.client.render26.ModGeoEntityRenderer;
import com.leon.saintsdragons.client.render26.SdLivingState;
import com.leon.saintsdragons.client.renderer.layer.NightEmissiveLayer;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.dragons.cindervane.Cindervane;
import net.minecraft.resources.Identifier;

public class CindervaneNightEmissiveLayer extends NightEmissiveLayer<Cindervane> {
    private static final Identifier EMISSIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/cindervane/cindervane_emissive.png");

    public CindervaneNightEmissiveLayer(ModGeoEntityRenderer<Cindervane, SdLivingState> renderer) {
        super(renderer);
    }

    @Override
    protected Identifier getEmissiveTexture(Cindervane animatable) {
        return EMISSIVE_TEXTURE;
    }
}
