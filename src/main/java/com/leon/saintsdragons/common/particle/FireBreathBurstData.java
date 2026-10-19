package com.leon.saintsdragons.common.particle;

import com.leon.saintsdragons.common.registry.ModParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record FireBreathBurstData(int dragonId) implements ParticleOptions {
    public static final MapCodec<FireBreathBurstData> CODEC = Codec.INT.fieldOf("dragon_id")
            .xmap(FireBreathBurstData::new, FireBreathBurstData::dragonId);

    @Override
    public @NotNull ParticleType<FireBreathBurstData> getType() {
        return ModParticles.FIRE_BREATH_BURST.get();
    }


}
