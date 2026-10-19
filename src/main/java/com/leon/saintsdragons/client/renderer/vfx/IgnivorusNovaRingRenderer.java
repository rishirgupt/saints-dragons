package com.leon.saintsdragons.client.renderer.vfx;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.effect.ignivorus.IgnivorusSkyfallRingEntity;
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

public class IgnivorusNovaRingRenderer extends LegacyEntityRenderer<IgnivorusSkyfallRingEntity> {

    private static final int TOTAL_FRAMES = 8;
    private static final Identifier[] TEXTURES = new Identifier[TOTAL_FRAMES];

    static {
        for (int i = 0; i < TOTAL_FRAMES; i++) {
            TEXTURES[i] = SaintsDragonsCommon.rl("textures/particle/shared/explosions/sharp_impact/sharp_impact" + i + ".png");
        }
    }

    public IgnivorusNovaRingRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(@NotNull IgnivorusSkyfallRingEntity entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {

        float scale = entity.getScale(partialTicks);
        float opacity = entity.getOpacity(partialTicks);

        if (opacity <= 0.001F) {
            return;
        }

        poseStack.pushPose();

        int frame = (entity.getAge() * TOTAL_FRAMES) / entity.getDuration();
        frame = Math.min(frame, TOTAL_FRAMES - 1);
        Identifier texture = TEXTURES[frame];

        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.entityTranslucent(texture));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float size = scale * 16.0F;

        renderHorizontalSquare(consumer, matrix, normalMatrix, size, opacity);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private void renderHorizontalSquare(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMatrix,
                                        float size, float opacity) {
        float half = size;

        Vector3f normalVec = new Vector3f(0.0F, 1.0F, 0.0F);
        normalMatrix.transform(normalVec);

        float y = 0.1F;

        consumer.addVertex(matrix, -half, y, -half)
                .setColor(1.0F, 1.0F, 1.0F, opacity)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(normalVec.x(), normalVec.y(), normalVec.z())
                ;

        consumer.addVertex(matrix, -half, y, half)
                .setColor(1.0F, 1.0F, 1.0F, opacity)
                .setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(normalVec.x(), normalVec.y(), normalVec.z())
                ;

        consumer.addVertex(matrix, half, y, half)
                .setColor(1.0F, 1.0F, 1.0F, opacity)
                .setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(normalVec.x(), normalVec.y(), normalVec.z())
                ;

        consumer.addVertex(matrix, half, y, -half)
                .setColor(1.0F, 1.0F, 1.0F, opacity)
                .setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(normalVec.x(), normalVec.y(), normalVec.z())
                ;
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull IgnivorusSkyfallRingEntity entity) {
        return TEXTURES[0];
    }
}
