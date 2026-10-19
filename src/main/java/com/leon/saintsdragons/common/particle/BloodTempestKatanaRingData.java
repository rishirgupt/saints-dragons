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

public record BloodTempestKatanaRingData(float yaw, float pitch, float scale, int duration)
        implements ParticleOptions {

    public static MapCodec<BloodTempestKatanaRingData> codec(
            @SuppressWarnings("unused") ParticleType<BloodTempestKatanaRingData> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("yaw").forGetter(BloodTempestKatanaRingData::yaw),
                Codec.FLOAT.fieldOf("pitch").forGetter(BloodTempestKatanaRingData::pitch),
                Codec.FLOAT.fieldOf("scale").forGetter(BloodTempestKatanaRingData::scale),
                Codec.INT.fieldOf("duration").forGetter(BloodTempestKatanaRingData::duration)
        ).apply(instance, BloodTempestKatanaRingData::new));
    }



    @Override
    public @NotNull ParticleType<BloodTempestKatanaRingData> getType() {
        return ModParticles.BLOOD_TEMPEST_SWORD_RING.get();
    }
}
