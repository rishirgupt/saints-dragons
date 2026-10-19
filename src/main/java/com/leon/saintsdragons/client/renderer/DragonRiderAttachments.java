package com.leon.saintsdragons.client.renderer;

import com.leon.saintsdragons.client.camera.RiderConfig;
import com.leon.saintsdragons.server.entity.base.RideableDragonBase;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Render-thread seat poses: where each rider sits on the animated dragon model for the current frame. */
public final class DragonRiderAttachments {
    private static final Map<UUID, FramePose> POSES = new HashMap<>();
    private static Object activeLevel;

    private DragonRiderAttachments() {}

    public static void clear() {
        POSES.clear();
        activeLevel = null;
    }

    public static boolean usesPoseCache(RideableDragonBase dragon) {
        return dragon.isVehicle() && !dragon.isRemoved() && RiderConfig.getSpec(dragon) != null
                && dragon.level() == Minecraft.getInstance().level
                && !EntityPreviewRenderContext.isRendering();
    }

    @Nullable
    public static SeatPose resolve(RideableDragonBase dragon, int seat, float partialTick) {
        if (!usesPoseCache(dragon) || !RiderConfig.getSpec(dragon).seats().containsKey(seat)) {
            return null;
        }
        FramePose pose = entry(dragon, partialTick);
        if (!pose.prepared && !pose.preparing) {
            pose.preparing = true;
            try {
                var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(dragon);
                if (renderer instanceof DragonGeoEntityRenderer<?> dragonRenderer) {
                    prepare(dragonRenderer, dragon, partialTick, pose);
                }
                pose.prepared = true;
            } finally {
                pose.preparing = false;
            }
        }
        return pose.seats.get(seat);
    }

    @SuppressWarnings("unchecked")
    private static <T extends RideableDragonBase> void prepare(DragonGeoEntityRenderer<T> renderer,
                                                               RideableDragonBase dragon, float partialTick,
                                                               FramePose pose) {
        renderer.prepareRiderAttachments((T) dragon, partialTick,
                (seat, boneTransform) -> storeSeat(dragon, seat, partialTick, boneTransform, pose));
    }

    private static void storeSeat(RideableDragonBase dragon, int seat, float partialTick, Matrix4f boneTransform,
                                  FramePose pose) {
        if (!boneTransform.isFinite() || Math.abs(boneTransform.determinant3x3()) < 1.0E-8F
                || pose.seats.containsKey(seat)) {
            return;
        }
        // Bone translation includes model scale; the rider keeps their own size.
        Matrix4f riderTransform = new Matrix4f(boneTransform).normalize3x3();
        riderTransform.translate(RiderConfig.getSeatOffset(dragon, seat));
        float yaw = Mth.rotLerp(partialTick, dragon.yBodyRotO, dragon.yBodyRot);
        riderTransform.rotateY((yaw + RiderConfig.getYawOffset(dragon, seat)) * Mth.DEG_TO_RAD);
        pose.seats.put(seat, new SeatPose(riderTransform));
    }

    /** The matrix that places a rider's model on its seat, relative to the rider's own render origin. */
    @Nullable
    public static Matrix4f riderModelTransform(Entity rider, float partialTick) {
        if (!(rider.getVehicle() instanceof RideableDragonBase dragon)) {
            return null;
        }
        SeatPose seat = resolve(dragon, dragon.getRiderSeatIndex(rider), partialTick);
        if (seat == null) {
            return null;
        }
        Vec3 delta = interpolatedPosition(dragon, partialTick).subtract(interpolatedPosition(rider, partialTick));
        return new Matrix4f().translation((float) delta.x, (float) delta.y, (float) delta.z).mul(seat.riderTransform);
    }

    public static void applyRiderTransform(PoseStack poses, Matrix4f transform) {
        poses.mulPose(transform);
    }

    public static Vec3 interpolatedPosition(Entity entity, float partialTick) {
        return new Vec3(Mth.lerp(partialTick, entity.xOld, entity.getX()),
                Mth.lerp(partialTick, entity.yOld, entity.getY()),
                Mth.lerp(partialTick, entity.zOld, entity.getZ()));
    }

    private static FramePose entry(RideableDragonBase dragon, float partialTick) {
        Object level = Minecraft.getInstance().level;
        if (activeLevel != level) {
            POSES.clear();
            activeLevel = level;
        }
        if (POSES.size() > 64) {
            POSES.clear();
        }
        FramePose pose = POSES.computeIfAbsent(dragon.getUUID(), ignored -> new FramePose());
        if (pose.tick != dragon.tickCount || pose.partialTick != partialTick || pose.entityId != dragon.getId()) {
            pose.tick = dragon.tickCount;
            pose.partialTick = partialTick;
            pose.entityId = dragon.getId();
            pose.prepared = false;
            pose.seats.clear();
        }
        return pose;
    }

    public static final class SeatPose {
        private final Matrix4f riderTransform;

        private SeatPose(Matrix4f riderTransform) {
            this.riderTransform = riderTransform;
        }

        public Vec3 eyePosition(RideableDragonBase dragon, Entity rider, float partialTick) {
            return worldPosition(dragon, partialTick, new Vec3(0, rider.getEyeHeight(), 0));
        }

        public Vec3 worldPosition(RideableDragonBase dragon, float partialTick, Vec3 riderLocalPosition) {
            Vector3f point = riderTransform.transformPosition(riderLocalPosition.toVector3f());
            return interpolatedPosition(dragon, partialTick).add(point.x, point.y, point.z);
        }

        public float cameraRoll(float yawDegrees, float pitchDegrees) {
            var look = new org.joml.Quaternionf().rotationYXZ(-yawDegrees * Mth.DEG_TO_RAD,
                    pitchDegrees * Mth.DEG_TO_RAD, 0);
            Vector3f up = riderTransform.transformDirection(0, 1, 0, new Vector3f()).normalize();
            Vector3f viewUp = look.transform(new Vector3f(0, 1, 0));
            Vector3f viewLeft = look.transform(new Vector3f(1, 0, 0));
            float x = up.dot(viewLeft);
            float y = up.dot(viewUp);
            // Looking nearly along the seat up axis (straight up on a level dragon) there is no horizon to follow:
            // the angle flips wildly there, so the roll fades out instead of being resolved from noise.
            float horizon = (float) Math.sqrt(x * x + y * y);
            float fade = Mth.clamp((horizon - 0.17F) / (0.60F - 0.17F), 0.0F, 1.0F);
            fade = fade * fade * (3.0F - 2.0F * fade);
            if (fade <= 0.0F) {
                return 0.0F;
            }
            return (float) Math.atan2(-x, y) * Mth.RAD_TO_DEG * fade;
        }
    }

    private static final class FramePose {
        private int tick = -1;
        private int entityId;
        private float partialTick;
        private boolean preparing;
        private boolean prepared;
        private final Map<Integer, SeatPose> seats = new HashMap<>();
    }
}
