package com.leon.saintsdragons.fabric.client.camera;

public final class DragonCameraState {
    private static float currentDragonRoll = 0.0f;
    private static boolean hasStableAngles = false;
    private static float stableYaw = 0.0f;
    private static float stablePitch = 0.0f;
    private static float stableRoll = 0.0f;
    private static float currentDiveRoll = 0.0f;

    private DragonCameraState() {
    }

    public static void setCurrentRoll(float roll) {
        // Follow the target along the short way round so a sudden change (take-off, looking straight up) eases in
        currentDragonRoll += net.minecraft.util.Mth.wrapDegrees(roll - currentDragonRoll) * 0.3f;
    }

    public static float getCurrentRoll() {
        return hasStableAngles ? stableRoll : currentDragonRoll;
    }

    public static void setDiveRoll(float roll) {
        currentDiveRoll = roll;
    }

    public static float getDiveRoll() {
        return currentDiveRoll;
    }

    public static void clearDiveRoll() {
        currentDiveRoll = 0.0f;
    }

    public static void setStableAngles(float yaw, float pitch, float roll) {
        hasStableAngles = true;
        stableYaw = yaw;
        stablePitch = pitch;
        stableRoll = roll;
        currentDragonRoll = roll;
    }

    /**
     * Whether the rider camera should push in to the first-person "zoom" view: while a ridden Raevyx fires
     * its beam, or a ridden Volitans uses its breath. Follows the same client option as the beam view.
     */
    public static boolean isBreathZoomActive(net.minecraft.world.entity.Entity vehicle) {
        if (!com.leon.saintsdragons.fabric.config.FabricClientConfigAccess.isRaevyxBeamFirstPersonEnabled()) {
            return false;
        }
        if (vehicle instanceof com.leon.saintsdragons.server.entity.dragons.raevyx.Raevyx raevyx) {
            return raevyx.isBeaming();
        }
        return vehicle instanceof com.leon.saintsdragons.server.entity.dragons.volitans.Volitans volitans
                && volitans.isBreathing();
    }

    public static void clearRoll() {
        currentDragonRoll = 0.0f;
        hasStableAngles = false;
    }
}
