package com.leon.saintsdragons.server.entity.handler;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Owner lookups for the EntityReference-based ownership API (1.21.5+). */
public final class EntityOwners {
    @Nullable
    public static UUID uuid(@Nullable OwnableEntity ownable) {
        if (ownable == null) {
            return null;
        }
        EntityReference<LivingEntity> owner = ownable.getOwnerReference();
        return owner != null ? owner.getUUID() : null;
    }

    private EntityOwners() {
    }
}
