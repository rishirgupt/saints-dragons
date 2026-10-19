package com.leon.saintsdragons.client.render26;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import org.jetbrains.annotations.Nullable;

/** Small adapters for effect code written against the 1.20.1 vertex and particle APIs. */
public final class Vfx {
    private Vfx() {
    }

    public static VertexConsumer vertex(VertexConsumer buffer, double x, double y, double z) {
        return buffer.addVertex((float) x, (float) y, (float) z);
    }

    public static void setColor(@Nullable Particle particle, float red, float green, float blue) {
        if (particle instanceof SingleQuadParticle quad) {
            quad.setColor(red, green, blue);
        }
    }
}
