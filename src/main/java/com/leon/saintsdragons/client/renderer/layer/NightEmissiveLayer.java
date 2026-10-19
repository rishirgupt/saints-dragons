package com.leon.saintsdragons.client.renderer.layer;

import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.geckolib.cache.model.BakedGeoModel;
import com.leon.saintsdragons.client.render26.ModGeoLayer;
import com.leon.saintsdragons.client.render26.ModGeoEntityRenderer;
import com.leon.saintsdragons.client.render26.SdLivingState;

public abstract class NightEmissiveLayer<T extends DragonEntity> extends ModGeoLayer<T> {
    private static final long DUSK_START = 12000L;
    private static final long NIGHT_FULL = 13000L;
    private static final long NIGHT_FADE = 22500L;
    private static final long DAWN_END = 23500L;

    protected NightEmissiveLayer(ModGeoEntityRenderer<T, SdLivingState> renderer) {
        super(renderer);
    }

    @Override
    public void render(@NotNull PoseStack poseStack,
                       T animatable,
                       BakedGeoModel bakedModel,
                       @NotNull RenderType renderType,
                       @NotNull MultiBufferSource bufferSource,
                       @NotNull VertexConsumer buffer,
                       float partialTick,
                       int packedLight,
                       int packedOverlay) {
        if (animatable.isBaby()) {
            return;
        }
        if (animatable.hasCustomTextureVariant() && !allowCustomTextureVariantEmissive(animatable)) {
            return;
        }

        float alpha = getNightAlpha(animatable);
        if (alpha <= 0.01F) {
            return;
        }

        Identifier texture = getEmissiveTexture(animatable);
        if (texture == null) {
            return;
        }

        RenderType glowType = RenderTypes.entityTranslucent(texture);
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowType);

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                glowType,
                glowBuffer,
                partialTick,
                0xF000F0,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                alpha
        );
    }

    @Nullable
    protected abstract Identifier getEmissiveTexture(T animatable);

    protected boolean allowCustomTextureVariantEmissive(T animatable) {
        return false;
    }

    private static float getNightAlpha(DragonEntity animatable) {
        long dayTime = animatable.level().getDefaultClockTime() % 24000L;
        if (dayTime < DUSK_START || dayTime > DAWN_END) {
            return 0.0F;
        }
        if (dayTime < NIGHT_FULL) {
            return Mth.clamp((dayTime - DUSK_START) / (float) (NIGHT_FULL - DUSK_START), 0.0F, 1.0F);
        }
        if (dayTime > NIGHT_FADE) {
            return Mth.clamp((DAWN_END - dayTime) / (float) (DAWN_END - NIGHT_FADE), 0.0F, 1.0F);
        }
        return 1.0F;
    }
}
