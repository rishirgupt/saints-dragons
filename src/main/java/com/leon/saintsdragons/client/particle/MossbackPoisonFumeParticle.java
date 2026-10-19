package com.leon.saintsdragons.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import com.leon.saintsdragons.client.render26.LegacyParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import com.leon.saintsdragons.client.render26.LegacyParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class MossbackPoisonFumeParticle extends LegacyParticle {
    protected MossbackPoisonFumeParticle(ClientLevel level, double x, double y, double z,
                                         double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.xd = xSpeed;
        this.yd = ySpeed + 0.025D;
        this.zd = zSpeed;
        this.gravity = -0.004F;
        this.friction = 0.92F;
        this.lifetime = 48 + this.random.nextInt(24);
        this.quadSize = 0.9F + this.random.nextFloat() * 0.55F;
        this.alpha = 0.78F;
        this.roll = (float) (Math.PI * 2.0D * this.random.nextDouble());
        this.oRoll = this.roll;
        this.setSprite(sprites.get(this.random));
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

        float progress = this.age / (float) this.lifetime;
        if (progress > 0.55F) {
            this.alpha = 0.78F * (1.0F - (progress - 0.55F) / 0.45F);
        }

        this.roll += 0.008F;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
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
            return new MossbackPoisonFumeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
