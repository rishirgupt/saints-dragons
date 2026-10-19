package com.leon.saintsdragons.client.renderer.layer.atroxiia;

import com.leon.saintsdragons.client.render26.ModGeoEntityRenderer;
import com.leon.saintsdragons.client.render26.SdLivingState;
import com.leon.saintsdragons.client.renderer.layer.NightEmissiveLayer;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.dragons.atroxiia.Atroxiia;
import net.minecraft.resources.Identifier;

public class AtroxiiaNightEmissiveLayer extends NightEmissiveLayer<Atroxiia> {
    private static final Identifier EMISSIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/atroxiia/atroxiia_emissive.png");

    public AtroxiiaNightEmissiveLayer(ModGeoEntityRenderer<Atroxiia, SdLivingState> renderer) {
        super(renderer);
    }

    @Override
    protected Identifier getEmissiveTexture(Atroxiia animatable) {
        return EMISSIVE_TEXTURE;
    }
}
