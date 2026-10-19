package com.leon.saintsdragons.common.particle;

import com.leon.saintsdragons.common.registry.ModParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Locale;

public record GroundDecalParticleData(boolean fissure, float yaw, float scale, int duration)
        implements ParticleOptions {
    /** Keeps the decal just above the supporting face without visibly floating. */
    public static final double GROUND_OFFSET = 0.002D;
    public static final float NORMAL_SCALE = 7.0F;
    public static final int NORMAL_DURATION = 34;


    public static MapCodec<GroundDecalParticleData> codec(
            @SuppressWarnings("unused") ParticleType<GroundDecalParticleData> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.fieldOf("fissure").forGetter(GroundDecalParticleData::fissure),
                Codec.FLOAT.fieldOf("yaw").forGetter(GroundDecalParticleData::yaw),
                Codec.FLOAT.fieldOf("scale").forGetter(GroundDecalParticleData::scale),
                Codec.INT.fieldOf("duration").forGetter(GroundDecalParticleData::duration)
        ).apply(instance, GroundDecalParticleData::new));
    }

    public static GroundDecalParticleData crack(float yaw) {
        return new GroundDecalParticleData(false, yaw, NORMAL_SCALE, NORMAL_DURATION);
    }

    public static GroundDecalParticleData fissure(float yaw, float scale, int duration) {
        return new GroundDecalParticleData(true, yaw, scale, duration);
    }



    @Override
    public @NotNull ParticleType<GroundDecalParticleData> getType() {
        return this.fissure ? ModParticles.GROUND_CRACK_FISSURE.get() : ModParticles.GROUND_CRACK.get();
    }
}
