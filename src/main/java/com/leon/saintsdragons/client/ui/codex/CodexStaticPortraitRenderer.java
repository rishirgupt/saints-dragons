package com.leon.saintsdragons.client.ui.codex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoObjectRenderer;
import com.geckolib.util.GeckoLibUtil;

import java.util.UUID;

final class CodexStaticPortraitRenderer extends GeoObjectRenderer<CodexStaticPortraitRenderer.Portrait> {
    CodexStaticPortraitRenderer() {
        super(new PortraitModel());
    }

    @Override
    public void preRender(PoseStack poseStack, Portrait portrait, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
    }

    @Override
    public void actuallyRender(PoseStack poseStack, Portrait portrait, BakedGeoModel model,
                               RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        for (GeoBone bone : model.topLevelBones()) {
            renderRecursively(poseStack, portrait, bone, renderType, bufferSource, buffer,
                    true, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    static final class Portrait implements GeoAnimatable {
        private final UUID dragonId;
        private final Identifier model;
        private final Identifier texture;
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        Portrait(UUID dragonId, Identifier model, Identifier texture) {
            this.dragonId = dragonId;
            this.model = model;
            this.texture = texture;
        }

        UUID dragonId() {
            return dragonId;
        }

        Identifier model() {
            return model;
        }

        Identifier texture() {
            return texture;
        }

        boolean matches(Identifier model, Identifier texture) {
            return this.model.equals(model) && this.texture.equals(texture);
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return cache;
        }

        @Override
        public double getTick(Object object) {
            return 0.0D;
        }
    }

    private static final class PortraitModel extends GeoModel<Portrait> {
        @Override
        public Identifier getModelResource(Portrait portrait) {
            return portrait.model();
        }

        @Override
        public Identifier getTextureResource(Portrait portrait) {
            return portrait.texture();
        }

        @Override
        public Identifier getAnimationResource(Portrait portrait) {
            return portrait.model();
        }
    }
}
