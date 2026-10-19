package com.leon.saintsdragons.fabric.mixin.fabric;

import com.leon.saintsdragons.client.camera.DragonFovEffects;
import com.leon.saintsdragons.client.renderer.DragonSeatAnchoredCamera;
import com.leon.saintsdragons.fabric.client.accessor.CameraAccessor;
import com.leon.saintsdragons.fabric.client.camera.CameraLeanData;
import com.leon.saintsdragons.fabric.client.camera.DragonCameraState;
import com.leon.saintsdragons.fabric.client.event.FabricClientEventHandler;
import com.leon.saintsdragons.fabric.config.FabricClientConfigAccess;
import com.leon.saintsdragons.server.entity.base.RideableDragonBase;
import com.leon.saintsdragons.server.entity.dragons.raevyx.Raevyx;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dragon riding camera on the 26.2 camera: anchors the view to the animated seat, orbits the third-person
 * camera around it, and rolls the view with the dragon. In 26.2 the view matrix is derived from the camera
 * rotation quaternion, so roll is applied there rather than on the level pose stack.
 */
@Mixin(Camera.class)
public abstract class CameraMixin implements CameraAccessor {
    @Shadow
    protected abstract void move(float forwards, float up, float right);

    @Shadow
    private float getMaxZoom(float cameraDist) {
        throw new AssertionError();
    }

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    private float xRot;

    @Shadow
    private float yRot;

    @Shadow
    private boolean detached;

    @Shadow
    private Entity entity;

    @Shadow
    @Final
    private Vector3f up;

    @Shadow
    @Final
    private Vector3f forwards;

    @Shadow
    @Final
    private Vector3f left;

    /** Roll in degrees folded into the next rotation quaternion; zero outside our own setRotation calls. */
    @Unique
    private float saintsdragons$pendingRoll = 0.0f;

    @Override
    public void saintsdragons$invokeMove(double distance, double vertical, double lateral) {
        // 1.20.1 moved along forwards/up/left; 26.2 takes forwards/up/right.
        this.move((float) distance, (float) vertical, (float) -lateral);
    }

    @Override
    public double saintsdragons$invokeGetMaxZoom(double distance) {
        return this.getMaxZoom((float) distance);
    }

    @Override
    public void saintsdragons$invokeSetPosition(double x, double y, double z) {
        this.setPosition(new Vec3(x, y, z));
    }

    @Override
    public void saintsdragons$invokeSetRotation(float yaw, float pitch) {
        this.setRotation(yaw, pitch);
    }

    @Override
    public float saintsdragons$invokeGetXRot() {
        return this.xRot;
    }

    @Override
    public float saintsdragons$invokeGetYRot() {
        return this.yRot;
    }

    @Inject(method = "alignWithEntity", at = @At("HEAD"))
    private void saintsdragons$preAlignSyncRoll(float partialTick, CallbackInfo ci) {
        Entity focusedEntity = this.entity;
        boolean thirdPerson = !net.minecraft.client.Minecraft.getInstance().options.getCameraType().isFirstPerson();
        if (focusedEntity == null || thirdPerson || !FabricClientConfigAccess.isFirstPersonBankingCameraEnabled()) {
            DragonCameraState.clearRoll();
            CameraLeanData.reset();
            return;
        }

        Entity vehicle = focusedEntity.getVehicle();
        if (!(vehicle instanceof RideableDragonBase dragon) || !DragonSeatAnchoredCamera.supports(dragon)
                || !saintsdragons$usesAerialBankingCamera(dragon)) {
            DragonCameraState.clearRoll();
            CameraLeanData.reset();
            return;
        }

        if (com.leon.saintsdragons.fabric.client.camera.DragonCameraState.isBreathZoomActive(dragon)) {
            DragonCameraState.clearRoll();
            CameraLeanData.reset();
            return;
        }

        var seat = DragonSeatAnchoredCamera.resolve(dragon, focusedEntity, partialTick);
        if (seat == null) {
            DragonCameraState.clearRoll();
            CameraLeanData.reset();
            return;
        }
        float rollDegrees = seat.cameraRoll(focusedEntity.getViewYRot(partialTick), focusedEntity.getViewXRot(partialTick));
        float pitchDegrees = Mth.lerp(partialTick, dragon.xRotO, dragon.getXRot());
        float yawSpeed = Mth.wrapDegrees(dragon.yBodyRot - dragon.yBodyRotO);
        CameraLeanData.updateTarget(-rollDegrees, pitchDegrees, yawSpeed, 1.0f);
        CameraLeanData.update();

        float cameraTilt = (float) CameraLeanData.getCameraTilt();
        DragonCameraState.setCurrentRoll(rollDegrees + cameraTilt);
    }

    // Set the orbit origin before vanilla performs its third-person obstruction checks.
    @Inject(method = "alignWithEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private void saintsdragons$thirdPersonSeatPivot(float partialTick, CallbackInfo ci) {
        Entity rider = this.entity;
        if (this.detached && rider != null && rider.getVehicle() instanceof RideableDragonBase dragon) {
            var seat = DragonSeatAnchoredCamera.resolve(dragon, rider, partialTick);
            if (seat != null) {
                this.setPosition(seat.eyePosition(dragon, rider, partialTick));
            }
        }
    }

    @Inject(method = "alignWithEntity", at = @At("RETURN"))
    private void saintsdragons$afterAlign(float partialTick, CallbackInfo ci) {
        Entity focusedEntity = this.entity;
        saintsdragons$applySeatAnchor(focusedEntity, this.detached, partialTick);
        FabricClientEventHandler.onComputeCamera((Camera) (Object) this, partialTick);
        FabricClientEventHandler.onComputeCameraAfterSeatAnchor((Camera) (Object) this, partialTick);
        saintsdragons$applyRoll(focusedEntity);
    }

    @Unique
    private void saintsdragons$applySeatAnchor(Entity focusedEntity, boolean thirdPerson, float partialTick) {
        if (focusedEntity == null || thirdPerson) {
            return;
        }

        Entity vehicle = focusedEntity.getVehicle();
        if (!(vehicle instanceof RideableDragonBase dragon) || !DragonSeatAnchoredCamera.supports(dragon)) {
            return;
        }

        var seat = DragonSeatAnchoredCamera.resolve(dragon, focusedEntity, partialTick);
        if (seat == null) {
            return;
        }

        this.setPosition(DragonSeatAnchoredCamera.computePivot(
                dragon,
                focusedEntity,
                seat,
                this.up,
                this.forwards,
                this.left,
                partialTick,
                CameraLeanData.getLeanX(),
                CameraLeanData.getLeanY(),
                CameraLeanData.getLeanZ()
        ));
    }

    @Unique
    private void saintsdragons$applyRoll(Entity focusedEntity) {
        if (focusedEntity == null || !(focusedEntity.getVehicle() instanceof RideableDragonBase dragon)) {
            return;
        }
        if (com.leon.saintsdragons.fabric.client.camera.DragonCameraState.isBreathZoomActive(dragon)) {
            return;
        }
        float roll = DragonCameraState.getDiveRoll();
        if (!this.detached
                && FabricClientConfigAccess.isFirstPersonBankingCameraEnabled()
                && DragonSeatAnchoredCamera.supports(dragon)) {
            roll += DragonCameraState.getCurrentRoll();
        }
        if (Math.abs(roll) < 0.01f) {
            return;
        }
        // The view rotation is the inverse of the camera rotation, so a view-space roll enters negated.
        this.saintsdragons$pendingRoll = -roll;
        try {
            this.setRotation(this.yRot, this.xRot);
        } finally {
            this.saintsdragons$pendingRoll = 0.0f;
        }
    }

    @ModifyArg(
            method = "setRotation",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;",
                    remap = false
            ),
            index = 2
    )
    private float saintsdragons$injectRollIntoCamera(float originalRoll) {
        float roll = this.saintsdragons$pendingRoll;
        return roll != 0.0f ? roll * Mth.DEG_TO_RAD : originalRoll;
    }

    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float saintsdragons$modifyFov(float incomingFov, float partialTicks) {
        return (float) DragonFovEffects.apply(incomingFov, partialTicks);
    }

    @Unique
    private static boolean saintsdragons$usesAerialBankingCamera(RideableDragonBase dragon) {
        return dragon.isFlying()
                || dragon.isTakeoff()
                || dragon.isLanding()
                || dragon.isHovering();
    }
}
