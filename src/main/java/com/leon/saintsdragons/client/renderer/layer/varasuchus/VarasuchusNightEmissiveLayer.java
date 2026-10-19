package com.leon.saintsdragons.client.renderer.layer.varasuchus;

import com.leon.saintsdragons.client.render26.ModGeoEntityRenderer;
import com.leon.saintsdragons.client.render26.SdLivingState;
import com.leon.saintsdragons.client.renderer.layer.NightEmissiveLayer;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.dragons.varasuchus.Varasuchus;
import net.minecraft.resources.Identifier;

public class VarasuchusNightEmissiveLayer extends NightEmissiveLayer<Varasuchus> {
    private static final Identifier EMISSIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/varasuchus/varasuchus_emissive.png");
    private static final Identifier VOID_KISSED_EMISSIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/varasuchus/varasuchus_void_kissed_emissive.png");

    public VarasuchusNightEmissiveLayer(ModGeoEntityRenderer<Varasuchus, SdLivingState> renderer) {
        super(renderer);
    }

    @Override
    protected Identifier getEmissiveTexture(Varasuchus animatable) {
        if (Varasuchus.VOID_KISSED_VARIANT_ID.equals(animatable.getTextureVariantId())) {
            return VOID_KISSED_EMISSIVE_TEXTURE;
        }
        return EMISSIVE_TEXTURE;
    }

    @Override
    protected boolean allowCustomTextureVariantEmissive(Varasuchus animatable) {
        return Varasuchus.VOID_KISSED_VARIANT_ID.equals(animatable.getTextureVariantId());
    }
}
