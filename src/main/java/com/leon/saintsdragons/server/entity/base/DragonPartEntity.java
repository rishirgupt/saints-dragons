package com.leon.saintsdragons.server.entity.base;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public interface DragonPartEntity {
    @Nullable Entity getDragonParent();
    int getPartIndex();
    float getDamageMultiplier();
    void updateBounds(AABB bounds);

    default boolean hurtParent(DamageSource source, float amount) {
        Entity parent = getDragonParent();
        Entity part = (Entity) this;
        if (parent == null || !parent.isAlive() || parent.isRemoved() || part.isRemoved()
                || parent.level() != part.level() || !Float.isFinite(amount) || amount <= 0
                || part.isInvulnerable()) return false;
        if (part.level().isClientSide()) return !parent.isInvulnerable();
        return parent.hurtOrSimulate(source, amount * getDamageMultiplier());
    }
}
