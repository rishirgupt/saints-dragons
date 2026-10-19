package com.leon.saintsdragons.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import com.leon.saintsdragons.client.render26.LegacyParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import com.leon.saintsdragons.client.render26.LegacyParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class DraconianNucleusParticle extends LegacyParticle {
    private static final float FADE_START_PROGRESS = 1.50F;
    private static final float FADE_END_PROGRESS = 1.65F;

    private final SpriteSet sprites;
    private final int baseLifetime;

    protected DraconianNucleusParticle(ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.friction = 0.96F;
        this.hasPhysics = false;
        this.baseLifetime = 100 + this.random.nextInt(40);
        this.lifetime = (int) Math.ceil(this.baseLifetime * FADE_END_PROGRESS);
        this.quadSize = 0.45F + this.random.nextFloat() * 0.2F;
        this.alpha = 0.9F;
        this.roll = this.random.nextFloat() * (float) (Math.PI * 2.0D);
        this.oRoll = this.roll;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = this.age / (float) this.baseLifetime;
        this.quadSize *= 1.006F;
        this.roll += 0.0025F;
        if (progress > FADE_START_PROGRESS) {
            float fade = 1.0F
                    - (progress - FADE_START_PROGRESS) / (FADE_END_PROGRESS - FADE_START_PROGRESS);
            this.alpha = 0.9F * Math.max(0.0F, fade);
        }

        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.zd *= this.friction;
        this.yd = Math.min(0.055D, this.yd + 0.00035D);
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public @NotNull LegacyParticleRenderType getRenderType() {
        return LegacyParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed, net.minecraft.util.RandomSource randomSource) {
            return new DraconianNucleusParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
