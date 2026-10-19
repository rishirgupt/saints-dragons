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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Locale;

public record RaevyxLightningChainData(float size, Vec3 startPos, Vec3 endPos) implements ParticleOptions {

    public static MapCodec<RaevyxLightningChainData> CODEC(@SuppressWarnings("unused") ParticleType<RaevyxLightningChainData> type) {
        return RecordCodecBuilder.mapCodec(b -> b.group(
                Codec.FLOAT.fieldOf("size").forGetter(RaevyxLightningChainData::size),
                Vec3.CODEC.fieldOf("startPos").forGetter(RaevyxLightningChainData::startPos),
                Vec3.CODEC.fieldOf("endPos").forGetter(RaevyxLightningChainData::endPos)
        ).apply(b, RaevyxLightningChainData::new));
    }



    @Override
    public @NotNull ParticleType<RaevyxLightningChainData> getType() {
        return ModParticles.LIGHTNING_CHAIN.get();
    }
}
