package com.leon.saintsdragons.client.render26;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Particle base that keeps the 1.20.1 contract: a particle picks a {@link LegacyParticleRenderType} and
 * writes its own vertices in {@link #render}. {@link LegacyParticleGroup} records those vertices each frame
 * and submits them to the 26.2 render queue.
 */
public abstract class LegacyParticle extends SingleQuadParticle {
    /** The particle group all of the mod's hand-drawn particles belong to. */
    public static final ParticleRenderType GROUP = new ParticleRenderType("saintsdragons:hand_drawn", "SD");

    protected LegacyParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, null);
    }

    protected LegacyParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd, null);
    }

    public abstract LegacyParticleRenderType getRenderType();

    /** Draws the particle; the default is the vanilla camera-facing quad. */
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        if (this.sprite == null) {
            return;
        }
        Vec3 cameraPos = camera.position();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());
        Quaternionf rotation = new Quaternionf(camera.rotation());
        if (this.roll != 0.0F) {
            rotation.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }
        float size = this.getQuadSize(partialTicks);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);
        quadVertex(buffer, rotation, x, y, z, 1.0F, -1.0F, size, u1, v1, light);
        quadVertex(buffer, rotation, x, y, z, 1.0F, 1.0F, size, u1, v0, light);
        quadVertex(buffer, rotation, x, y, z, -1.0F, 1.0F, size, u0, v0, light);
        quadVertex(buffer, rotation, x, y, z, -1.0F, -1.0F, size, u0, v1, light);
    }

    private void quadVertex(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z,
                            float xOffset, float yOffset, float size, float u, float v, int light) {
        Vector3f corner = new Vector3f(xOffset, yOffset, 0.0F).rotate(rotation).mul(size).add(x, y, z);
        buffer.addVertex(corner.x(), corner.y(), corner.z())
                .setUv(u, v)
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                .setLight(light);
    }

    // ---- 1.20.1 names kept for the particle sources ----

    /** World light at the particle; override for full-bright or custom lighting. */
    protected int getLightColor(float partialTick) {
        return super.getLightCoords(partialTick);
    }

    @Override
    protected final int getLightCoords(float partialTick) {
        return getLightColor(partialTick);
    }

    public void pickSprite(SpriteSet sprites) {
        this.setSprite(sprites.get(this.random));
    }

    @Override
    public ParticleRenderType getGroup() {
        return getRenderType() == LegacyParticleRenderType.NO_RENDER ? ParticleRenderType.NO_RENDER : GROUP;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }
}
