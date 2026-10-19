package com.leon.saintsdragons.client.renderer.vfx;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.ability.abilities.ignivorus.IgnivorusUltimateAbility;
import com.leon.saintsdragons.server.entity.dragons.ignivorus.Ignivorus;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public final class IgnivorusSkyfallSphereRenderer {
    private static final RenderType SHELL = BeamRenderTypes.translucent(
            SaintsDragonsCommon.rl("textures/particle/shared/misc/blank.png"));

    private IgnivorusSkyfallSphereRenderer() {
    }

    public static void render(Ignivorus dragon, PoseStack poses, MultiBufferSource buffers, float partialTick) {
        if (!dragon.isAlive() || dragon.isInvisible()) return;
        float elapsed = dragon.getSkyfallElapsedTicks(partialTick);
        float remaining = IgnivorusUltimateAbility.SKYFALL_EXPLOSION_TICK - elapsed;
        if (elapsed < 0.0F || remaining <= 0.0F || remaining >= 6.0F) return;
        float progress = 1.0F - remaining / 6.0F;
        float radius = Mth.lerp(1.0F - (1.0F - progress) * (1.0F - progress), 2.0F, 38.0F);
        float fadeIn = Mth.clamp(progress / 0.15F, 0.0F, 1.0F);
        float fadeOut = 1.0F - progress;
        float alpha = 0.45F * fadeIn * fadeOut * fadeOut;
        VertexConsumer buffer = buffers.getBuffer(SHELL);
        poses.pushPose();
        poses.translate(0.0D, 20.0D, 0.0D);
        for (int ring = 0; ring < 16; ring++) {
            float latitude0 = -Mth.HALF_PI + Mth.PI * ring / 16.0F;
            float latitude1 = -Mth.HALF_PI + Mth.PI * (ring + 1) / 16.0F;
            for (int segment = 0; segment < 32; segment++) {
                float longitude0 = Mth.TWO_PI * segment / 32.0F;
                float longitude1 = Mth.TWO_PI * (segment + 1) / 32.0F;
                vertex(buffer, poses.last(), latitude0, longitude0, radius, alpha, 0, 0);
                vertex(buffer, poses.last(), latitude1, longitude0, radius, alpha, 0, 1);
                vertex(buffer, poses.last(), latitude1, longitude1, radius, alpha, 1, 1);
                vertex(buffer, poses.last(), latitude0, longitude1, radius, alpha, 1, 0);
            }
        }
        poses.popPose();
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float latitude, float longitude,
                               float radius, float alpha, float u, float v) {
        float nx = Mth.cos(latitude) * Mth.cos(longitude);
        float ny = Mth.sin(latitude);
        float nz = Mth.cos(latitude) * Mth.sin(longitude);
        buffer.addVertex(pose.pose(), nx * radius, ny * radius, nz * radius)
                .setColor(1.0F, 0.65F, 0.18F, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(com.leon.saintsdragons.client.render26.Normals.x(pose.normal(), nx, ny, nz), com.leon.saintsdragons.client.render26.Normals.y(pose.normal(), nx, ny, nz), com.leon.saintsdragons.client.render26.Normals.z(pose.normal(), nx, ny, nz));
    }
}
