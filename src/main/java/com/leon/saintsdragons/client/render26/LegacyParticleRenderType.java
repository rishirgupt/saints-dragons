package com.leon.saintsdragons.client.render26;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.jetbrains.annotations.Nullable;

/**
 * The particle "render types" the mod's particles choose between, mapped to 26.2 render types that draw
 * particle-format quads from the particle atlas.
 */
public final class LegacyParticleRenderType {
    public static final LegacyParticleRenderType PARTICLE_SHEET_TRANSLUCENT = new LegacyParticleRenderType(
            "PARTICLE_SHEET_TRANSLUCENT", RenderType.create("saintsdragons_particle_translucent",
            RenderSetup.builder(RenderPipelines.TRANSLUCENT_PARTICLE)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_PARTICLES)
                    .useLightmap()
                    .sortOnUpload()
                    .createRenderSetup()));

    public static final LegacyParticleRenderType PARTICLE_SHEET_OPAQUE = new LegacyParticleRenderType(
            "PARTICLE_SHEET_OPAQUE", RenderType.create("saintsdragons_particle_opaque",
            RenderSetup.builder(RenderPipelines.OPAQUE_PARTICLE)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_PARTICLES)
                    .useLightmap()
                    .createRenderSetup()));

    /** Alpha blended, unculled, depth tested but not written: overlapping glow layers stay visible. */
    public static final LegacyParticleRenderType TRANSLUCENT_NO_DEPTH_WRITE = new LegacyParticleRenderType(
            "TRANSLUCENT_NO_DEPTH_WRITE", RenderType.create("saintsdragons_particle_no_depth_write",
            RenderSetup.builder(RenderPipelines.WEATHER_NO_DEPTH_WRITE)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_PARTICLES)
                    .useLightmap()
                    .sortOnUpload()
                    .createRenderSetup()));

    public static final LegacyParticleRenderType NO_RENDER = new LegacyParticleRenderType("NO_RENDER", null);
    public static final LegacyParticleRenderType CUSTOM = PARTICLE_SHEET_TRANSLUCENT;
    public static final LegacyParticleRenderType PARTICLE_SHEET_LIT = PARTICLE_SHEET_OPAQUE;

    private final String name;
    private final @Nullable RenderType renderType;

    private LegacyParticleRenderType(String name, @Nullable RenderType renderType) {
        this.name = name;
        this.renderType = renderType;
    }

    public @Nullable RenderType renderType() {
        return renderType;
    }

    @Override
    public String toString() {
        return name;
    }
}
