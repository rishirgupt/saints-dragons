package com.leon.saintsdragons.server.entity.effect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/** Swept entity hit test for effect entities that are not vanilla projectiles. */
public final class EffectHits {
    /** Same pick margin vanilla uses for projectiles. */
    private static final float PICK_MARGIN = 0.3F;

    @Nullable
    public static EntityHitResult entityHit(Level level, Entity source, Vec3 start, Vec3 end, AABB bounds,
                                            Predicate<Entity> filter) {
        return ProjectileUtil.getEntityHitResult(level, source, start, end, bounds, filter, PICK_MARGIN);
    }

    private EffectHits() {
    }
}
