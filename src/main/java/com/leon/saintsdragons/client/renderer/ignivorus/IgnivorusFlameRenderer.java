package com.leon.saintsdragons.client.renderer.ignivorus;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.effect.ignivorus.IgnivorusFlameEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.leon.saintsdragons.client.render26.LegacyEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;


public class IgnivorusFlameRenderer extends LegacyEntityRenderer<IgnivorusFlameEntity> {

    private static final int TOTAL_FRAMES = 5;
    private static final float FLAME_RENDER_SCALE = 0.65F;
    private static final float SPAWN_START_SCALE_FACTOR = 0.18F;
    private static final float SPAWN_GROWTH_TICKS = 5.0F;
    private static final Identifier[] TEXTURES = new Identifier[TOTAL_FRAMES];

    static {
        for (int i = 0; i < TOTAL_FRAMES; i++) {
            TEXTURES[i] = SaintsDragonsCommon.rl("textures/entity/ignivorus/fireparticle" + i + ".png");
        }
    }
    public IgnivorusFlameRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(@NotNull IgnivorusFlameEntity entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        int age = entity.getAge();
        int frame = (age / 2) % TOTAL_FRAMES;

        Identifier texture = TEXTURES[frame];
        float baseScale = entity.getScale() * FLAME_RENDER_SCALE;
        float ageWithPartial = Math.max(0.0F, age + partialTicks);
        float spawnProgress = Mth.clamp(ageWithPartial / SPAWN_GROWTH_TICKS, 0.0F, 1.0F);
        float spawnScaleFactor = Mth.lerp(spawnProgress, SPAWN_START_SCALE_FACTOR, 1.0F);
        float scale = baseScale * spawnScaleFactor;
        float alpha = 1.0F;
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(net.minecraft.client.Minecraft.getInstance().gameRenderer.mainCamera().rotation());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderTypes.entityCutout(texture));
        renderBillboard(vertexConsumer, matrix4f, matrix3f, packedLight, alpha);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private void renderBillboard(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f, int packedLight, float alpha) {
        float size = 1.0F;

        addVertex(consumer, matrix4f, matrix3f, -size, -size, 0.0F, 0.0F, 1.0F, alpha);
        addVertex(consumer, matrix4f, matrix3f, -size, size, 0.0F, 0.0F, 0.0F, alpha);
        addVertex(consumer, matrix4f, matrix3f, size, size, 0.0F, 1.0F, 0.0F, alpha);
        addVertex(consumer, matrix4f, matrix3f, size, -size, 0.0F, 1.0F, 1.0F, alpha);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f,
                          float x, float y, float z, float u, float v, float alpha) {
        consumer.addVertex(matrix4f, x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(com.leon.saintsdragons.client.render26.Normals.x(matrix3f, 0.0F, 0.0F, 1.0F), com.leon.saintsdragons.client.render26.Normals.y(matrix3f, 0.0F, 0.0F, 1.0F), com.leon.saintsdragons.client.render26.Normals.z(matrix3f, 0.0F, 0.0F, 1.0F))
                ;
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull IgnivorusFlameEntity entity) {
        return TEXTURES[0];
    }
}