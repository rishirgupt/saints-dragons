package com.leon.saintsdragons.client.renderer.layer.volitans;

import com.leon.saintsdragons.client.render26.ModGeoEntityRenderer;
import com.leon.saintsdragons.client.render26.SdLivingState;
import com.leon.saintsdragons.client.renderer.layer.NightEmissiveLayer;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.dragons.volitans.Volitans;
import net.minecraft.resources.Identifier;

public class VolitansNightEmissiveLayer extends NightEmissiveLayer<Volitans> {
    private static final Identifier EMISSIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/volitans/volitans_emissive.png");
    private static final Identifier BLOODSHOT_EMISSIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/volitans/volitans_bloodshot_emissive.png");

    public VolitansNightEmissiveLayer(ModGeoEntityRenderer<Volitans, SdLivingState> renderer) {
        super(renderer);
    }

    @Override
    protected Identifier getEmissiveTexture(Volitans animatable) {
        if (animatable.getTextureVariant() == Volitans.VARIANT_BLOODSHOT) {
            return BLOODSHOT_EMISSIVE_TEXTURE;
        }
        return EMISSIVE_TEXTURE;
    }
}
