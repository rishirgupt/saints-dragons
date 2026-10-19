package com.leon.saintsdragons.server.entity.effect;

import com.leon.saintsdragons.server.entity.base.DragonEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Feedback for a dragon attack that connected: a burst of particles on the target and an impact sound,
 * so a hit reads clearly even on targets that are far away or hidden behind the dragon.
 */
public final class DragonHitIndicator {
    private DragonHitIndicator() {
    }

    public static void onDamaged(LivingEntity target, DamageSource source, float damage) {
        if (!(target.level() instanceof ServerLevel level) || damage <= 0.0F) {
            return;
        }
        boolean fromDragon = source.getEntity() instanceof DragonEntity || source.getDirectEntity() instanceof DragonEntity;
        if (!fromDragon || source.getEntity() == target) {
            return;
        }
        // Beams and breaths damage every few ticks; keep the indicator readable instead of constant.
        long now = level.getGameTime();
        Long last = LAST_HITS.get(target);
        if (last != null && now - last < 6L) {
            return;
        }
        LAST_HITS.put(target, now);

        double x = target.getX();
        double y = target.getY(0.6D);
        double z = target.getZ();
        double spread = Math.max(0.25D, target.getBbWidth() * 0.4D);
        int crits = Mth.clamp(Mth.ceil(damage * 1.5F), 6, 40);
        int hearts = Mth.clamp(Mth.ceil(damage * 0.5F), 2, 16);
        level.sendParticles(ParticleTypes.CRIT, x, y, z, crits, spread, spread, spread, 0.35D);
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, hearts, spread, spread, spread, 0.15D);
        Entity attacker = source.getEntity() instanceof DragonEntity ? source.getEntity() : source.getDirectEntity();
        SimpleParticleType element = elementOf(attacker);
        if (element != null) {
            // A burst in the attacking dragon's element, scaled with the hit, so each attack reads as its own.
            int motes = Mth.clamp(Mth.ceil(damage * 2.0F), 8, 48);
            level.sendParticles(element, x, y, z, motes, spread * 1.4D, spread * 1.4D, spread * 1.4D, 0.08D);
            if (damage >= 12.0F) {
                level.sendParticles(ParticleTypes.POOF, x, target.getY() + 0.1D, z, 10, spread * 2.0D, 0.05D, spread * 2.0D, 0.06D);
            }
        }
        if (damage >= 8.0F) {
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 1.0F,
                0.85F + level.getRandom().nextFloat() * 0.3F);
    }

    private static SimpleParticleType elementOf(Entity attacker) {
        if (attacker == null) {
            return null;
        }
        return switch (attacker.getClass().getSimpleName()) {
            case "Ignivorus", "Cindervane" -> ParticleTypes.FLAME;
            case "Raevyx" -> ParticleTypes.ELECTRIC_SPARK;
            case "Atroxiia", "Volitans" -> ParticleTypes.SNOWFLAKE;
            case "Nulljaw" -> ParticleTypes.REVERSE_PORTAL;
            case "Varasuchus" -> ParticleTypes.ENCHANTED_HIT;
            case "Stegonaut" -> ParticleTypes.CLOUD;
            default -> null;
        };
    }

    private static final java.util.Map<LivingEntity, Long> LAST_HITS = new java.util.WeakHashMap<>();
}
