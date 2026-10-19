package com.leon.saintsdragons.client.renderer.volitans;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.leon.saintsdragons.client.model.volitans.VolitansBurrowMoundModel;
import com.leon.saintsdragons.client.render26.BlockCubes;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import com.leon.saintsdragons.client.render26.ObjectGeoRenderer;
import com.leon.saintsdragons.client.render26.SdEntityState;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.effect.volitans.VolitansBurrowMoundEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The mound a burrowing Volitans pushes up. The "pillars" and "formations" bone groups are drawn as the
 * block being dug through; the rest of the model uses its own texture and fades with the mound.
 */
public class VolitansBurrowMoundRenderer extends ObjectGeoRenderer<VolitansBurrowMoundEntity> {
    private static final Identifier TEXTURE = SaintsDragonsCommon.rl("textures/objects/volitans/burrow_mound.png");
    private static final float MODEL_FORWARD_YAW_OFFSET = 0.0F;
    private int passLight;
    private float passOpacity = 1.0F;

    public VolitansBurrowMoundRenderer(EntityRendererProvider.Context context) {
        super(context, new VolitansBurrowMoundModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    protected float getDeathMaxRotation(@NotNull VolitansBurrowMoundEntity entity) {
        return 0.0F;
    }

    @Override
    public void render(@NotNull VolitansBurrowMoundEntity entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        passLight = packedLight;
        passOpacity = entity.getOpacity(partialTick);
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getVisualYaw() + MODEL_FORWARD_YAW_OFFSET));
    }

    @Override
    protected boolean shouldSubmitModel(VolitansBurrowMoundEntity entity, float partialTick) {
        return entity.getOpacity(partialTick) > 0.001F;
    }

    @Override
    protected int modelColor(VolitansBurrowMoundEntity entity, int renderColor, float partialTick) {
        float alpha = ARGB.alphaFloat(renderColor) * entity.getOpacity(partialTick);
        return ARGB.color(Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F), renderColor);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<SdEntityState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        // The block-material groups are replaced by block models, so their own cubes are not drawn.
        for (GeoBone top : renderPassInfo.model().topLevelBones()) {
            hideBlockMaterial(top, false, snapshots);
        }
    }

    private static void hideBlockMaterial(GeoBone bone, boolean inherited, BoneSnapshots snapshots) {
        boolean blockMaterial = inherited || usesBlockMaterial(bone);
        if (blockMaterial) {
            snapshots.get(bone.name()).ifPresent(snapshot -> snapshot.skipRender(true));
        }
        for (GeoBone child : bone.children()) {
            hideBlockMaterial(child, blockMaterial, snapshots);
        }
    }

    @Override
    protected boolean wantsBoneTransforms(VolitansBurrowMoundEntity entity) {
        return passOpacity > 0.001F;
    }

    @Override
    protected void onBonePose(VolitansBurrowMoundEntity entity, GeoBone bone, PoseStack poseStack,
                              MultiBufferSource bufferSource) {
        if (!inBlockMaterial(bone)) {
            return;
        }
        BlockCubes.render(bone, entity.getBlockState(), poseStack, bufferSource, passLight,
                OverlayTexture.NO_OVERLAY, passOpacity);
    }

    private static boolean inBlockMaterial(GeoBone bone) {
        for (GeoBone current = bone; current != null; current = current.parent()) {
            if (usesBlockMaterial(current)) {
                return true;
            }
        }
        return false;
    }

    private static boolean usesBlockMaterial(GeoBone bone) {
        String name = bone.name();
        return "pillars".equals(name) || "formations".equals(name);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull VolitansBurrowMoundEntity entity) {
        return TEXTURE;
    }

    @Override
    public RenderType getRenderType(VolitansBurrowMoundEntity animatable, Identifier texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityTranslucent(texture);
    }
}
