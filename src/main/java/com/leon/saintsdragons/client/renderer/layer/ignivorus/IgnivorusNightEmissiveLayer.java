package com.leon.saintsdragons.client.renderer.layer.ignivorus;

import com.leon.saintsdragons.client.render26.ModGeoEntityRenderer;
import com.leon.saintsdragons.client.render26.SdLivingState;
import com.leon.saintsdragons.client.renderer.layer.NightEmissiveLayer;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.dragons.ignivorus.Ignivorus;
import net.minecraft.resources.Identifier;

public class IgnivorusNightEmissiveLayer extends NightEmissiveLayer<Ignivorus> {
    private static final Identifier EMISSIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/ignivorus/ignivorus_emissive.png");
    private static final Identifier CRIMSON_EMISSIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/ignivorus/crimson_ignivorus_emissive.png");
    private static final Identifier CRIMSON_FEMALE_EMISSIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/ignivorus/crimson_ignivorus_female_emissive.png");

    public IgnivorusNightEmissiveLayer(ModGeoEntityRenderer<Ignivorus, SdLivingState> renderer) {
        super(renderer);
    }

    @Override
    protected Identifier getEmissiveTexture(Ignivorus animatable) {
        if (animatable.getTextureVariant() == Ignivorus.VARIANT_CRIMSON) {
            return animatable.isFemale() ? CRIMSON_FEMALE_EMISSIVE_TEXTURE : CRIMSON_EMISSIVE_TEXTURE;
        }
        return EMISSIVE_TEXTURE;
    }
}
