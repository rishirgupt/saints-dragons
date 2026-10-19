package com.leon.saintsdragons.fabric.client.renderer;

import com.leon.saintsdragons.fabric.entity.part.FabricDragonPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class FabricDragonPartRenderer extends EntityRenderer<FabricDragonPart> {
    private static final Identifier EMPTY_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/misc/white.png");

    public FabricDragonPartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FabricDragonPart entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        // Intentionally empty: hitbox-only entity.
    }

    @Override
    public Identifier getTextureLocation(FabricDragonPart entity) {
        return EMPTY_TEXTURE;
    }
}
