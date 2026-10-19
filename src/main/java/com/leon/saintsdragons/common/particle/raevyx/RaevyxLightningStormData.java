package com.leon.saintsdragons.common.particle.raevyx;

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

public record RaevyxLightningStormData(float size, boolean nightGold) implements ParticleOptions {
    public RaevyxLightningStormData(float size) {
        this(size, false);
    }


    public static MapCodec<RaevyxLightningStormData> CODEC(@SuppressWarnings("unused") ParticleType<RaevyxLightningStormData> type) {
        return RecordCodecBuilder.mapCodec(b -> b.group(
                Codec.FLOAT.fieldOf("size").forGetter(RaevyxLightningStormData::size)
        ).apply(b, size -> new RaevyxLightningStormData(size, isNightGoldType(type))));
    }

    private static boolean isNightGoldType(ParticleType<RaevyxLightningStormData> type) {
        return type == ModParticles.LIGHTNING_STORM_NIGHT_GOLD.get();
    }



    @Override
    public @NotNull ParticleType<RaevyxLightningStormData> getType() {
        return this.nightGold ? ModParticles.LIGHTNING_STORM_NIGHT_GOLD.get() : ModParticles.LIGHTNING_STORM.get();
    }
}
