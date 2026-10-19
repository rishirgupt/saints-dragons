package com.leon.saintsdragons.client.renderer.layer.raevyx;

import com.leon.saintsdragons.client.render26.ModGeoEntityRenderer;
import com.leon.saintsdragons.client.render26.SdLivingState;
import com.leon.saintsdragons.client.renderer.layer.NightEmissiveLayer;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.dragons.raevyx.Raevyx;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class RaevyxNightEmissiveLayer extends NightEmissiveLayer<Raevyx> {
    private static final Identifier EMISSIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/raevyx/raevyx_emissive.png");
    private static final Identifier NIGHT_GOLD_EMISSIVE_TEXTURE =
            SaintsDragonsCommon.rl("textures/entity/raevyx/raevyx_night_gold_emissive.png");

    public RaevyxNightEmissiveLayer(ModGeoEntityRenderer<Raevyx, SdLivingState> renderer) {
        super(renderer);
    }

    @Override
    @Nullable
    protected Identifier getEmissiveTexture(Raevyx animatable) {
        if (animatable.getTextureVariant() == Raevyx.VARIANT_NIGHT_GOLD) {
            return NIGHT_GOLD_EMISSIVE_TEXTURE;
        }
        return EMISSIVE_TEXTURE;
    }
}
