package com.leon.saintsdragons.client.renderer.layer;

import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import com.geckolib.cache.model.BakedGeoModel;
import com.leon.saintsdragons.client.render26.ModGeoLayer;
import com.leon.saintsdragons.client.render26.ModGeoEntityRenderer;
import com.leon.saintsdragons.client.render26.SdLivingState;

import java.util.function.Predicate;

public final class DragonEquipmentLayer<T extends DragonEntity> extends ModGeoLayer<T> {
    private final Predicate<T> visible;
    private final Identifier texture;

    public DragonEquipmentLayer(ModGeoEntityRenderer<T, SdLivingState> renderer, Predicate<T> visible, Identifier texture) {
        super(renderer);
        this.visible = visible;
        this.texture = texture;
    }

    @Override
    public void render(
            @NotNull PoseStack poseStack,
            T animatable,
            BakedGeoModel bakedModel,
            @NotNull RenderType renderType,
            @NotNull MultiBufferSource bufferSource,
            @NotNull VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        if (!visible.test(animatable)) {
            return;
        }

        RenderType equipmentRenderType = RenderTypes.entityCutout(texture);
        VertexConsumer equipmentBuffer = bufferSource.getBuffer(equipmentRenderType);
        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                equipmentRenderType,
                equipmentBuffer,
                partialTick,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}
