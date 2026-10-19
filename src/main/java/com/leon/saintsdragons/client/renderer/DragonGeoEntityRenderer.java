package com.leon.saintsdragons.client.renderer;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.RenderUtil;
import com.leon.saintsdragons.client.camera.RiderConfig;
import com.leon.saintsdragons.client.render26.ModGeoEntityRenderer;
import com.leon.saintsdragons.client.render26.MultiBufferSource;
import com.leon.saintsdragons.client.render26.RecordingBufferSource;
import com.leon.saintsdragons.client.render26.SdLivingState;
import com.leon.saintsdragons.client.ui.DraconicCodexScreen;
import com.leon.saintsdragons.server.entity.base.RideableDragonBase;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.BiConsumer;

public abstract class DragonGeoEntityRenderer<T extends RideableDragonBase> extends ModGeoEntityRenderer<T, SdLivingState> {
    /** Set on render states built for GUI previews; those must not feed world-space locators or effects. */
    private static final DataTicket<Boolean> PREVIEW = DataTicket.create("saintsdragons_preview", Boolean.class);

    /** The baked model of the pass being finished; valid inside {@link #afterDragonRender}. */
    protected BakedGeoModel lastBakedModel;

    protected DragonGeoEntityRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    public float getMotionAnimThreshold(T animatable) {
        return 0.000001f;
    }

    @Override
    protected float getDeathMaxRotation(T entity) {
        return 0.0F;
    }

    @Override
    public SdLivingState createRenderState(T animatable, @Nullable Void relatedObject) {
        return new SdLivingState();
    }

    @Override
    public @Nullable RenderType getRenderType(T animatable, Identifier texture,
                                              @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityCutout(texture);
    }

    @Override
    protected float getShadowRadius(T entity) {
        return entity.isBaby() ? getBabyShadowRadius(entity) : getAdultShadowRadius(entity);
    }

    protected abstract float getBabyShadowRadius(T entity);

    protected abstract float getAdultShadowRadius(T entity);

    protected LocatorSpec[] locatorSpecs(T entity) {
        return new LocatorSpec[0];
    }

    /** World-space effects drawn after the dragon; the pose stack is at the origin of the dragon. */
    protected void afterDragonRender(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
    }

    public static boolean isPreview(GeoRenderState renderState) {
        return Boolean.TRUE.equals(renderState.getGeckolibData(PREVIEW));
    }

    @Override
    public void captureDefaultRenderState(T animatable, @Nullable Void relatedObject, SdLivingState renderState,
                                          float partialTick) {
        super.captureDefaultRenderState(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(PREVIEW, EntityPreviewRenderContext.isRendering()
                || DraconicCodexScreen.RENDERING_IN_GUI.get());
    }

    @Override
    public void preRenderPass(RenderPassInfo<SdLivingState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        T entity = entityOf(renderPassInfo.renderState());
        if (entity == null) {
            return;
        }
        for (LocatorSpec spec : locatorSpecs(entity)) {
            trackBone(spec.boneName());
        }
        RiderConfig.RiderSpec riderSpec = RiderConfig.getSpec(entity);
        if (riderSpec != null) {
            for (RiderConfig.SeatSpec seat : riderSpec.seats().values()) {
                trackBone(seat.boneName());
            }
        }
    }

    @Override
    protected void afterModelSubmitted(T entity, RenderPassInfo<SdLivingState> renderPassInfo,
                                       SubmitNodeCollector renderTasks, float partialTick) {
        if (isPreview(renderPassInfo.renderState())) {
            return;
        }
        this.lastBakedModel = getPassModel();
        sampleLocators(entity);
        RecordingBufferSource recorder = new RecordingBufferSource();
        afterDragonRender(entity, renderPassInfo.poseStack(), recorder, partialTick);
        recorder.flush(renderTasks);
    }

    protected void sampleLocators(T entity) {
        for (LocatorSpec spec : locatorSpecs(entity)) {
            trackBoneToLocators(entity, spec.boneName(), spec.x(), spec.y(), spec.z(), spec.locatorNames());
        }
        RiderConfig.RiderSpec riderSpec = RiderConfig.getSpec(entity);
        if (riderSpec != null) {
            for (RiderConfig.SeatSpec seat : riderSpec.seats().values()) {
                if (seat.locatorName() != null) {
                    Vector3f offset = seat.locatorOffset();
                    trackBoneToLocators(entity, seat.boneName(), offset.x(), offset.y(), offset.z(),
                            seat.locatorName());
                }
            }
        }
    }

    protected void trackBoneToLocators(T entity, String boneName, float x, float y, float z, String... locatorNames) {
        Vec3 world = transformLocator(boneName, x, y, z);
        if (world == null) {
            return;
        }
        for (String locatorName : locatorNames) {
            entity.setClientLocatorPosition(locatorName, world);
        }
    }

    /**
     * Evaluates the animated pose without drawing and reports each seat bone's transform relative to the
     * dragon's interpolated position. Used for the rider and the seat-anchored camera, which are needed
     * before the dragon itself is submitted.
     */
    public void prepareRiderAttachments(T entity, float partialTick, BiConsumer<Integer, Matrix4f> seats) {
        RiderConfig.RiderSpec spec = RiderConfig.getSpec(entity);
        if (spec == null) {
            return;
        }
        SdLivingState state = createRenderState(entity, partialTick);
        PoseStack root = new PoseStack();
        RenderPassInfo<SdLivingState> pass = RenderPassInfo.create(this, state, root, new CameraRenderState(), false);
        BakedGeoModel baked = pass.model();
        Vec3 renderOffset = getRenderOffset(state);
        root.translate(renderOffset.x, renderOffset.y, renderOffset.z);
        scaleModelForRender(pass, 1.0F, 1.0F);
        adjustRenderPose(pass);
        pass.captureModelRenderPose();
        pass.renderPosed(() -> spec.seats().forEach((seatIndex, seat) -> baked.getBone(seat.boneName()).ifPresent(bone -> {
            root.pushPose();
            try {
                RenderUtil.transformToBone(root, bone);
                seats.accept(seatIndex, new Matrix4f(root.last().pose()));
            } finally {
                root.popPose();
            }
        })));
    }

    public record LocatorSpec(String boneName, float x, float y, float z, String... locatorNames) {
    }
}
