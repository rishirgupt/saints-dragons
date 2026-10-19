package com.leon.saintsdragons.client.render26;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

/**
 * Holds the mod's hand-drawn particles. Each frame every particle writes its vertices into a recording
 * buffer for its render type; the recording is then submitted as custom geometry.
 */
public final class LegacyParticleGroup extends ParticleGroup<LegacyParticle> {
    public LegacyParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
        RecordingBufferSource recorder = new RecordingBufferSource();
        for (LegacyParticle particle : this.particles) {
            RenderType renderType = particle.getRenderType().renderType();
            if (renderType == null) {
                continue;
            }
            try {
                particle.render(recorder.getBuffer(renderType), camera, partialTickTime);
            } catch (Throwable throwable) {
                CrashReport report = CrashReport.forThrowable(throwable, "Rendering Particle");
                CrashReportCategory category = report.addCategory("Particle being rendered");
                category.setDetail("Particle", particle::toString);
                throw new ReportedException(report);
            }
        }
        return (submitNodeCollector, cameraState) -> recorder.flush(submitNodeCollector);
    }
}
