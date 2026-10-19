package com.leon.saintsdragons.fabric.mixin.fabric;

import com.leon.saintsdragons.client.renderer.EntityPreviewRenderContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Marks entity render states built for GUI previews so world-space effects and seat poses are skipped. */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("HEAD"))
    private static void saintsdragons$beginEntityPreview(LivingEntity entity,
                                                          CallbackInfoReturnable<EntityRenderState> ci) {
        EntityPreviewRenderContext.begin();
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN"))
    private static void saintsdragons$endEntityPreview(LivingEntity entity,
                                                        CallbackInfoReturnable<EntityRenderState> ci) {
        EntityPreviewRenderContext.end();
    }
}
