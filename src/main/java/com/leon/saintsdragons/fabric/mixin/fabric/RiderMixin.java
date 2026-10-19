package com.leon.saintsdragons.fabric.mixin.fabric;

import com.leon.saintsdragons.client.renderer.DragonRiderAttachments;
import com.leon.saintsdragons.client.renderer.EntityPreviewRenderContext;
import com.leon.saintsdragons.fabric.client.accessor.SeatTransformHolder;
import com.leon.saintsdragons.server.entity.dragons.nulljaw.Nulljaw;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records, while a player's render state is built, where the animated dragon seat puts that player. */
@Mixin(AvatarRenderer.class)
public abstract class RiderMixin {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL")
    )
    private void saintsdragons$seatRiderOnDragon(Avatar player, AvatarRenderState state, float partialTick,
                                                 CallbackInfo ci) {
        SeatTransformHolder holder = (SeatTransformHolder) state;
        holder.saintsdragons$setSeatTransform(null);
        if (EntityPreviewRenderContext.isRendering()) {
            return;
        }
        if (player.getVehicle() instanceof Nulljaw) {
            // Nulljaw riders stand on its back instead of sitting.
            state.isPassenger = false;
        }
        holder.saintsdragons$setSeatTransform(DragonRiderAttachments.riderModelTransform(player, partialTick));
        if (player instanceof net.minecraft.client.player.AbstractClientPlayer clientPlayer) {
            ((com.leon.saintsdragons.fabric.client.accessor.AfterimageHolder) state).saintsdragons$setAfterimages(
                    com.leon.saintsdragons.client.renderer.vfx.BloodTempestAfterimageTrail.snapshotsFor(
                            clientPlayer, partialTick));
        }
    }
}
