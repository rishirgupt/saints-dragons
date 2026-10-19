package com.leon.saintsdragons.client.renderer.raevyx;

import com.leon.saintsdragons.server.entity.effect.raevyx.RaevyxLightningChainEntity;
import com.leon.saintsdragons.client.render26.LegacyEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer for LightningChainEntity.
 * Since this entity is purely visual and manages its own particles,
 * we don't need to render anything here - just prevent the null renderer crash.
 */
@Environment(EnvType.CLIENT)
public class RaevyxLightningChainRenderer extends LegacyEntityRenderer<RaevyxLightningChainEntity> {

    public RaevyxLightningChainRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull RaevyxLightningChainEntity entity) {
        // Return a dummy texture since we don't actually render the entity
        return Identifier.fromNamespaceAndPath("minecraft", "textures/entity/lightning_bolt.png");
    }
}
