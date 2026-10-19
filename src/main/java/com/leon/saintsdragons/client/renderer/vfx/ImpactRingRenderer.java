package com.leon.saintsdragons.client.renderer.vfx;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.effect.ImpactRingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.leon.saintsdragons.client.render26.LegacyEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ImpactRingRenderer extends LegacyEntityRenderer<ImpactRingEntity> {
    private static final int TOTAL_FRAMES = 4;
    private static final Identifier[] TEXTURES = new Identifier[TOTAL_FRAMES];

    static {
        for (int i = 0; i < TOTAL_FRAMES; i++) {
            TEXTURES[i] = SaintsDragonsCommon.rl("textures/particle/shared/rings/impact_ring/impact_ring" + i + ".png");
        }
    }

    public ImpactRingRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(@NotNull ImpactRingEntity entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        float opacity = entity.getOpacity(partialTicks);
        if (opacity <= 0.001F) {
            return;
        }

        int frame = (entity.getAge() * TOTAL_FRAMES) / entity.getDuration();
        frame = Math.min(frame, TOTAL_FRAMES - 1);
        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.entityTranslucent(TEXTURES[frame]));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        float size = entity.getScale(partialTicks) * 8.0F;

        renderHorizontalSquare(consumer, matrix, normalMatrix, size, opacity);
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private void renderHorizontalSquare(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMatrix,
                                        float size, float opacity) {
        Vector3f normal = new Vector3f(0.0F, 1.0F, 0.0F);
        normalMatrix.transform(normal);
        float y = ImpactRingEntity.RENDER_PLANE_Y;

        consumer.addVertex(matrix, -size, y, -size)
                .setColor(1.0F, 1.0F, 1.0F, opacity)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(normal.x(), normal.y(), normal.z())
                ;
        consumer.addVertex(matrix, -size, y, size)
                .setColor(1.0F, 1.0F, 1.0F, opacity)
                .setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(normal.x(), normal.y(), normal.z())
                ;
        consumer.addVertex(matrix, size, y, size)
                .setColor(1.0F, 1.0F, 1.0F, opacity)
                .setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(normal.x(), normal.y(), normal.z())
                ;
        consumer.addVertex(matrix, size, y, -size)
                .setColor(1.0F, 1.0F, 1.0F, opacity)
                .setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(normal.x(), normal.y(), normal.z())
                ;
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull ImpactRingEntity entity) {
        return TEXTURES[0];
    }
}
