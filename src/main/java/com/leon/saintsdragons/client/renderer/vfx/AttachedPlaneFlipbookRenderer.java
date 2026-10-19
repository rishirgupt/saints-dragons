package com.leon.saintsdragons.client.renderer.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.LightCoordsUtil;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class AttachedPlaneFlipbookRenderer {
    private static final long PULSE_SEED = 0x9E3779B97F4A7C15L;

    private AttachedPlaneFlipbookRenderer() {
    }

    public static final class State {
        private final List<Pulse> pulses = new ArrayList<>();
        private float nextSpawnTime;
        private float lastTime = Float.NaN;
        private boolean emitting;
        private long sequence;

        public void update(float time, boolean emit, long seed, int frameCount, Style style) {
            if (!Float.isNaN(lastTime) && time < lastTime) {
                pulses.clear();
                emitting = false;
            }
            float lifetime = frameCount * Math.max(0.05F, style.frameTicks());
            pulses.removeIf(pulse -> time - pulse.birthTime() >= lifetime);
            if (emit && frameCount > 0) {
                if (!emitting || time - nextSpawnTime >= lifetime) {
                    nextSpawnTime = time;
                }
                while (nextSpawnTime <= time) {
                    RandomSource random = RandomSource.create(seed ^ PULSE_SEED * ++sequence);
                    pulses.add(new Pulse(nextSpawnTime, random.nextFloat() * Mth.TWO_PI));
                    nextSpawnTime += Math.max(0.5F, style.emissionIntervalTicks());
                }
            }
            emitting = emit;
            lastTime = time;
        }

        public boolean isActive() {
            return !pulses.isEmpty();
        }
    }

    private record Pulse(float birthTime, float angle) {
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffers,
                              Identifier[] textures, State state, Style style, float time,
                              float red, float green, float blue) {
        for (Pulse pulse : state.pulses) {
            renderOnce(poseStack, buffers, textures, style, time - pulse.birthTime(), pulse.angle(), red, green, blue);
        }
    }

    /** Render an externally timed one-shot, without adding it to the repeating emitter. */
    public static void renderOnce(PoseStack poseStack, MultiBufferSource buffers,
                                  Identifier[] textures, Style style, float age, float angle,
                                  float red, float green, float blue) {
        float frameTicks = Math.max(0.05F, style.frameTicks());
        float lifetime = textures.length * frameTicks;
        float size = style.halfSize();
        float alpha = Mth.clamp(style.alpha(), 0.0F, 1.0F);
        if (size <= 0.0F || alpha <= 0.0F || age < 0.0F || age >= lifetime) {
            return;
        }
        PoseStack.Pose pose = poseStack.last();
        int frame = Math.min(textures.length - 1, Mth.floor(age / frameTicks));
        VertexConsumer consumer = buffers.getBuffer(RenderTypes.entityTranslucent(textures[frame]));
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        // Same plane, winding and unculled render type as the attached wind effects.
        vertex(consumer, pose.pose(), pose.normal(), -size, -size, style.forwardOffset(), cos, sin, 0, 1, red, green, blue, alpha);
        vertex(consumer, pose.pose(), pose.normal(), size, -size, style.forwardOffset(), cos, sin, 1, 1, red, green, blue, alpha);
        vertex(consumer, pose.pose(), pose.normal(), size, size, style.forwardOffset(), cos, sin, 1, 0, red, green, blue, alpha);
        vertex(consumer, pose.pose(), pose.normal(), -size, size, style.forwardOffset(), cos, sin, 0, 0, red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                               float x, float y, float z, float cos, float sin, float u, float v,
                               float red, float green, float blue, float alpha) {
        consumer.addVertex(matrix, x * cos - y * sin, x * sin + y * cos, z)
                .setColor(red, green, blue, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(com.leon.saintsdragons.client.render26.Normals.x(normal, 0.0F, 0.0F, 1.0F), com.leon.saintsdragons.client.render26.Normals.y(normal, 0.0F, 0.0F, 1.0F), com.leon.saintsdragons.client.render26.Normals.z(normal, 0.0F, 0.0F, 1.0F));
    }

    public record Style(float halfSize, float alpha, float frameTicks,
                        float emissionIntervalTicks, float forwardOffset) {
    }
}
