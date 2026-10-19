package com.leon.saintsdragons.fabric.mixin.fabric;

import com.leon.saintsdragons.client.renderer.vfx.BloodTempestAfterimageRenderContext;
import com.leon.saintsdragons.client.renderer.vfx.BloodTempestAfterimageTrail;
import com.leon.saintsdragons.fabric.client.accessor.AfterimageHolder;
import com.leon.saintsdragons.fabric.client.accessor.SeatTransformHolder;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Two player-model hooks: seating a dragon rider on the animated seat, and drawing the Blood Tempest
 * dodge afterimages as translucent, tinted copies of the body behind the player.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState> {
    private static final String SUBMIT =
            "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V";

    @Shadow
    public abstract Identifier getTextureLocation(S state);

    @Shadow
    public abstract void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                CameraRenderState camera);

    @Inject(method = SUBMIT, at = @At("HEAD"))
    private void saintsdragons$transformRiderOnDragon(S state, PoseStack poseStack,
                                                      SubmitNodeCollector submitNodeCollector,
                                                      CameraRenderState camera, CallbackInfo ci) {
        if (BloodTempestAfterimageRenderContext.isActive()) {
            // The pose already carries the seat transform from the main pass.
            return;
        }
        Matrix4f transform = ((SeatTransformHolder) state).saintsdragons$getSeatTransform();
        if (transform != null) {
            poseStack.mulPose(transform);
        }
    }

    @Inject(method = SUBMIT, at = @At("TAIL"))
    private void saintsdragons$submitAfterimages(S state, PoseStack poseStack,
                                                 SubmitNodeCollector submitNodeCollector,
                                                 CameraRenderState camera, CallbackInfo ci) {
        if (BloodTempestAfterimageRenderContext.isActive() || !(state instanceof AvatarRenderState avatar)) {
            return;
        }
        List<BloodTempestAfterimageTrail.RenderedSnapshot> afterimages =
                ((AfterimageHolder) state).saintsdragons$getAfterimages();
        if (afterimages.isEmpty()) {
            return;
        }
        // The main pass is already submitted; strip what should not be repeated in the ghost copies.
        avatar.nameTag = null;
        avatar.scoreText = null;
        avatar.heldOnHead.clear();
        if (state instanceof ArmedEntityRenderState armed) {
            armed.rightHandItemState.clear();
            armed.leftHandItemState.clear();
        }
        if (state instanceof HumanoidRenderState humanoid) {
            humanoid.headEquipment = ItemStack.EMPTY;
            humanoid.chestEquipment = ItemStack.EMPTY;
            humanoid.legsEquipment = ItemStack.EMPTY;
            humanoid.feetEquipment = ItemStack.EMPTY;
        }
        for (BloodTempestAfterimageTrail.RenderedSnapshot snapshot : afterimages) {
            Vec3 offset = snapshot.offset();
            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y, offset.z);
            BloodTempestAfterimageRenderContext.begin(snapshot.alpha());
            try {
                this.submit(state, poseStack, submitNodeCollector, camera);
            } finally {
                BloodTempestAfterimageRenderContext.end();
                poseStack.popPose();
            }
        }
    }

    @ModifyReturnValue(method = "getModelTint", at = @At("RETURN"))
    private int saintsdragons$tintAfterimage(int original) {
        if (!BloodTempestAfterimageRenderContext.isActive()) {
            return original;
        }
        return ARGB.colorFromFloat(BloodTempestAfterimageRenderContext.alpha(),
                BloodTempestAfterimageRenderContext.red(), BloodTempestAfterimageRenderContext.green(),
                BloodTempestAfterimageRenderContext.blue());
    }

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void saintsdragons$useAfterimageRenderType(S state, boolean isBodyVisible, boolean forceTransparent,
                                                       boolean appearGlowing,
                                                       CallbackInfoReturnable<RenderType> cir) {
        if (BloodTempestAfterimageRenderContext.isActive()) {
            cir.setReturnValue(RenderTypes.entityTranslucent(this.getTextureLocation(state)));
        }
    }
}
