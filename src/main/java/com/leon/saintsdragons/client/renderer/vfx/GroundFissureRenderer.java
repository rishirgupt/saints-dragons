package com.leon.saintsdragons.client.renderer.vfx;

import com.leon.saintsdragons.server.entity.effect.GroundFissureEntity;
import com.leon.saintsdragons.client.render26.LegacyEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * The fissure entity is a gameplay controller only; its visual is a particle.
 */
public final class GroundFissureRenderer extends LegacyEntityRenderer<GroundFissureEntity> {
    public GroundFissureRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull GroundFissureEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
