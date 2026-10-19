package com.leon.saintsdragons.server.ai.dragonbrain.behaviour;

import com.leon.saintsdragons.server.ai.navigation.PathNavigateGround;
import com.leon.saintsdragons.server.entity.base.RideableDragonBase;
import com.leon.saintsdragons.server.entity.base.RideableFlyingDragon;
import com.leon.saintsdragons.server.entity.interfaces.SemiAquaticDragon;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

final class DragonOwnerFollowWaterHandoff {
    @Nullable
    private RideableDragonBase dragon;
    @Nullable
    private PathNavigateGround navigation;
    private float originalWaterMalus;
    private float originalWaterBorderMalus;

    void activate(RideableDragonBase candidate) {
        if (dragon == candidate && navigation != null) {
            return;
        }
        release();
        if (!(candidate instanceof SemiAquaticDragon) || !candidate.canSwim()) {
            return;
        }
        if (candidate instanceof RideableFlyingDragon flyingDragon) {
            flyingDragon.switchToGroundNavigation();
        }
        if (!(candidate.getNavigation() instanceof PathNavigateGround groundNavigation)) {
            return;
        }

        dragon = candidate;
        navigation = groundNavigation;
        originalWaterMalus = candidate.getPathfindingMalus(PathType.WATER);
        originalWaterBorderMalus = candidate.getPathfindingMalus(PathType.WATER_BORDER);
        groundNavigation.setWaterEntryAllowed(true);
        candidate.setPathfindingMalus(PathType.WATER, 0.0F);
        candidate.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
    }

    void release() {
        if (dragon == null || navigation == null) {
            return;
        }
        navigation.setWaterEntryAllowed(false);
        dragon.setPathfindingMalus(PathType.WATER, originalWaterMalus);
        dragon.setPathfindingMalus(PathType.WATER_BORDER, originalWaterBorderMalus);
        dragon = null;
        navigation = null;
    }

    boolean isActive() {
        return dragon != null;
    }
}
