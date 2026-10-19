package com.leon.saintsdragons.fabric.client.particle;

import com.leon.saintsdragons.client.init.CommonParticleFactories;
import com.leon.saintsdragons.client.render26.LegacyParticle;
import com.leon.saintsdragons.client.render26.LegacyParticleGroup;
import net.fabricmc.fabric.api.client.particle.v1.ParticleGroupRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public final class FabricParticleRegistry {
    private FabricParticleRegistry() {
    }

    public static void register() {
        // The hand-drawn particles live in their own group, drawn after the vanilla quad particles.
        ParticleGroupRegistry.register(LegacyParticle.GROUP, LegacyParticleGroup::new);
        ParticleGroupRegistry.registerOrdering(ParticleRenderType.SINGLE_QUADS, LegacyParticle.GROUP);
        CommonParticleFactories.register(new CommonParticleFactories.Registrar() {
            @Override
            public <T extends ParticleOptions> void register(ParticleType<T> type,
                                                            CommonParticleFactories.SpriteFactory<T> factory) {
                ParticleProviderRegistry.getInstance().register(type, factory::create);
            }
        });
    }
}
