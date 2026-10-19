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

public record SonicRingData(float yaw, float pitch, float scale, int duration) implements ParticleOptions {

    public static MapCodec<SonicRingData> CODEC(@SuppressWarnings("unused") ParticleType<SonicRingData> type) {
        return RecordCodecBuilder.mapCodec(b -> b.group(
                Codec.FLOAT.fieldOf("yaw").forGetter(SonicRingData::yaw),
                Codec.FLOAT.fieldOf("pitch").forGetter(SonicRingData::pitch),
                Codec.FLOAT.fieldOf("scale").forGetter(SonicRingData::scale),
                Codec.INT.fieldOf("duration").forGetter(SonicRingData::duration)
        ).apply(b, SonicRingData::new));
    }



    @Override
    public @NotNull ParticleType<SonicRingData> getType() {
        return ModParticles.RAEVYX_SONIC_RING.get();
    }
}
