package com.leon.saintsdragons.common.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

/** Particle type for the mod's data-carrying particles; the network codec is derived from the map codec. */
public final class CodecParticleType<T extends ParticleOptions> extends ParticleType<T> {
    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    public CodecParticleType(boolean overrideLimiter, Function<ParticleType<T>, MapCodec<T>> codecFactory) {
        super(overrideLimiter);
        this.codec = codecFactory.apply(this);
        this.streamCodec = ByteBufCodecs.fromCodecWithRegistries(this.codec.codec());
    }

    @Override
    public MapCodec<T> codec() {
        return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
        return streamCodec;
    }
}
