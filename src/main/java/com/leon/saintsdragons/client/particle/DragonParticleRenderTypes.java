package com.leon.saintsdragons.client.particle;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.leon.saintsdragons.client.render26.LegacyParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public final class DragonParticleRenderTypes {
    /**
     * Standard alpha blending with depth testing, but without writing the
     * particle quad into the depth buffer. This lets overlapping glow layers
     * remain independently visible while still being hidden by world geometry.
     */
    public static final LegacyParticleRenderType TRANSLUCENT_NO_DEPTH_WRITE = new LegacyParticleRenderType() {
        @Override
        public void begin(BufferBuilder buffer, TextureManager textureManager) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.setShader(DragonParticleShaders::getImpactGlowShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }

        @Override
        public String toString() {
            return "SAINTS_DRAGONS_TRANSLUCENT_NO_DEPTH_WRITE";
        }
    };

    private DragonParticleRenderTypes() {
    }
}
