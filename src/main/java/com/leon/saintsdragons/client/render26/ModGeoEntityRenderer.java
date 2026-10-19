package com.leon.saintsdragons.client.render26;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * Entity renderer for the mod's GeckoLib entities on the 26.2 render-state pipeline.
 *
 * It restores what the 1.20.1 renderers relied on: the model can pose bones procedurally with access to the
 * entity, chosen bones have their render matrices captured for effects and locators, and subclasses get a
 * hook after the model has been submitted.
 */
public class ModGeoEntityRenderer<T extends Entity & GeoAnimatable, R extends EntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<T, R> {
    private ToDoubleFunction<T> shadow = entity -> 0.0D;
    private ToDoubleFunction<T> scale = entity -> 1.0D;
    private Function<Identifier, RenderType> renderType = RenderTypes::entityCutout;

    /** Camera-relative matrices of tracked bones for the pass being submitted, positioned at each pivot. */
    private final Map<String, Matrix4f> boneMatrices = new HashMap<>();
    private final List<String> extraTrackedBones = new ArrayList<>();
    private boolean bonesCaptured;
    private @Nullable Vec3 passCameraPos;
    private @Nullable BakedGeoModel passModel;

    public ModGeoEntityRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }

    public ModGeoEntityRenderer<T, R> shadow(ToDoubleFunction<T> shadow) {
        this.shadow = shadow;
        return this;
    }

    public ModGeoEntityRenderer<T, R> shadow(float radius) {
        return shadow(entity -> radius);
    }

    public ModGeoEntityRenderer<T, R> scale(ToDoubleFunction<T> scale) {
        this.scale = scale;
        return this;
    }

    public ModGeoEntityRenderer<T, R> renderType(Function<Identifier, RenderType> renderType) {
        this.renderType = renderType;
        return this;
    }

    // ---- hooks for subclasses (the 1.20.1 renderer API, adapted) ----

    protected float getRenderScale(T entity) {
        return (float) scale.applyAsDouble(entity);
    }

    protected float getShadowRadius(T entity) {
        return (float) shadow.applyAsDouble(entity);
    }

    /** Bones whose render matrices should be captured each pass. */
    protected String[] trackedBoneNames() {
        return new String[0];
    }

    /** Also track this bone (used by layers that need a bone position). */
    public void trackBone(String boneName) {
        if (!extraTrackedBones.contains(boneName)) {
            extraTrackedBones.add(boneName);
        }
    }

    /** Called once per pass after the model and its layers were submitted; the pose stack is at the entity origin. */
    protected void afterModelSubmitted(T entity, RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks,
                                       float partialTick) {
    }

    /** The entity of the pass, or null when the render state was not built by this renderer. */
    @SuppressWarnings("unchecked")
    public @Nullable T entityOf(GeoRenderState renderState) {
        return (T) renderState.getGeckolibData(SdTickets.ENTITY);
    }

    // ---- 1.20.1 renderer hooks; the base versions do nothing, so "super" calls in ported code are harmless ----

    /**
     * Runs before the model is submitted, with the pose stack at the entity origin. Pose-stack changes made
     * here also apply to the model; geometry drawn into the buffer source is submitted after the model.
     */
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
    }

    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                          @Nullable com.mojang.blaze3d.vertex.VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, float red, float green,
                          float blue, float alpha) {
    }

    public @Nullable RenderType getRenderType(T animatable, Identifier texture,
                                              @Nullable MultiBufferSource bufferSource, float partialTick) {
        return renderType.apply(texture);
    }

    protected float getDeathMaxRotation(T entity) {
        return 90.0F;
    }

    /** A texture that replaces the one chosen by the model, or null. */
    public @Nullable Identifier getTextureLocation(T animatable) {
        return null;
    }

    private @Nullable RecordingBufferSource passRecorder;
    /** Pose after model scaling and before entity rotations: the pose 1.20.1 layers were given. */
    private @Nullable PoseStack.Pose passLayerPose;

    @Override
    public void adjustRenderPose(RenderPassInfo<R> renderPassInfo) {
        passLayerPose = renderPassInfo.poseStack().last().copy();
        super.adjustRenderPose(renderPassInfo);
    }

    /** The pose stack entry layers should draw with (entity origin, model scale, no rotation). */
    @Nullable PoseStack.Pose layerPose() {
        return passLayerPose;
    }

    // ---- bone tracking ----

    /** Camera-relative render matrix of a tracked bone in this pass, as 1.20.1 GeckoLib tracked it. */
    public @Nullable Matrix4f getBoneMatrix(String boneName) {
        return boneMatrices.get(boneName);
    }

    public Map<String, Matrix4f> getBoneMatrices() {
        return boneMatrices;
    }

    public @Nullable BakedGeoModel getPassModel() {
        return passModel;
    }

    public Vec3 getPassCameraPosition() {
        return passCameraPos == null ? Vec3.ZERO : passCameraPos;
    }

    /** World position of a tracked bone (its pivot), as 1.20.1 GeckoLib reported it. */
    public @Nullable Vec3 getBoneWorldPosition(String boneName) {
        return transformLocator(boneName, 0.0F, 0.0F, 0.0F);
    }

    /** World position of a point given in pixels relative to the pivot of a tracked bone. */
    public @Nullable Vec3 transformLocator(String boneName, float px, float py, float pz) {
        Matrix4f matrix = boneMatrices.get(boneName);
        if (matrix == null || passCameraPos == null) {
            return null;
        }
        Vector3f out = matrix.transformPosition(px / 16.0F, py / 16.0F, pz / 16.0F, new Vector3f());
        return new Vec3(passCameraPos.x + out.x(), passCameraPos.y + out.y(), passCameraPos.z + out.z());
    }

    /** Whether {@link #onBoneTransform} should be called for the bones of this pass. */
    protected boolean wantsBoneTransforms(T entity) {
        return false;
    }

    /**
     * Called for every visible bone of the posed model with its transform relative to the entity origin
     * (world axes), as 1.20.1 renderers computed it inside renderRecursively.
     */
    protected void onBoneTransform(T entity, GeoBone bone, Matrix4f entityRelative) {
    }

    /**
     * Called for every bone (hidden ones included) with the live pose stack positioned at that bone, so a
     * renderer can draw something of its own in place of the bone geometry.
     */
    protected void onBonePose(T entity, GeoBone bone, PoseStack poseStack, MultiBufferSource bufferSource) {
    }

    /** Tint and opacity the model is drawn with. */
    protected int modelColor(T entity, int renderColor, float partialTick) {
        return renderColor;
    }

    /** Return false to leave the model itself out of this pass. */
    protected boolean shouldSubmitModel(T entity, float partialTick) {
        return true;
    }

    private void visitBone(T entity, PoseStack poseStack, GeoBone bone, Matrix4f inverseBase) {
        poseStack.pushPose();
        RenderUtil.prepMatrixForBone(poseStack, bone);
        if (passRecorder != null) {
            onBonePose(entity, bone, poseStack, passRecorder);
        }
        if (bone.frameSnapshot == null || !bone.frameSnapshot.isHidden()) {
            onBoneTransform(entity, bone, new Matrix4f(inverseBase).mul(poseStack.last().pose()));
        }
        if (bone.frameSnapshot == null || !bone.frameSnapshot.areChildrenHidden()) {
            for (GeoBone child : bone.children()) {
                visitBone(entity, poseStack, child, inverseBase);
            }
        }
        poseStack.popPose();
    }

    private void captureBoneMatrices(RenderPassInfo<R> renderPassInfo) {
        T passEntity = entityOf(renderPassInfo.renderState());
        if (passEntity != null && wantsBoneTransforms(passEntity)) {
            Float modelScale = renderPassInfo.renderState().getOrDefaultGeckolibData(SdTickets.MODEL_SCALE, 1.0F);
            Matrix4f inverseBase = new Matrix4f(renderPassInfo.getPreRenderMatrixState())
                    .scale(modelScale == null ? 1.0F : modelScale).invert();
            renderPassInfo.renderPosed(() -> {
                for (GeoBone bone : renderPassInfo.model().topLevelBones()) {
                    visitBone(passEntity, renderPassInfo.poseStack(), bone, inverseBase);
                }
            });
        }
        String[] tracked = trackedBoneNames();
        if (tracked.length == 0 && extraTrackedBones.isEmpty()) {
            return;
        }
        BakedGeoModel model = renderPassInfo.model();
        PoseStack poseStack = renderPassInfo.poseStack();
        renderPassInfo.renderPosed(() -> {
            for (String name : tracked) {
                captureBone(model, poseStack, name);
            }
            for (String name : extraTrackedBones) {
                captureBone(model, poseStack, name);
            }
        });
    }

    private void captureBone(BakedGeoModel model, PoseStack poseStack, String name) {
        if (boneMatrices.containsKey(name)) {
            return;
        }
        model.getBone(name).ifPresent(bone -> {
            poseStack.pushPose();
            // 1.20.1 GeckoLib tracked a bone at its pivot, after its own rotation and scale.
            RenderUtil.transformToBone(poseStack, bone);
            boneMatrices.put(name, new Matrix4f(poseStack.last().pose()));
            poseStack.popPose();
        });
    }

    /** Applies every ancestor and the bone itself, ending at the model origin as transformed by the chain. */
    public static void applyBoneChain(PoseStack poseStack, GeoBone bone) {
        if (bone.parent() != null) {
            applyBoneChain(poseStack, bone.parent());
        }
        RenderUtil.prepMatrixForBone(poseStack, bone);
    }

    // ---- 1.20.1-style layer support ----

    private @Nullable RenderPassInfo<R> layerPass;
    private @Nullable SubmitNodeCollector layerTasks;

    void beginLayer(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        this.layerPass = renderPassInfo;
        this.layerTasks = renderTasks;
    }

    void endLayer() {
        this.layerPass = null;
        this.layerTasks = null;
    }

    /**
     * Draws the posed model again with another render type, light and tint, on top of the base pass.
     * Only valid while a {@link ModGeoLayer} is rendering; the model, pose stack and buffer arguments are
     * kept for source compatibility and ignored.
     */
    public void reRender(@Nullable BakedGeoModel model, @Nullable PoseStack poseStack,
                         @Nullable MultiBufferSource bufferSource, T animatable, RenderType renderType,
                         @Nullable com.mojang.blaze3d.vertex.VertexConsumer buffer, float partialTick,
                         int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        RenderPassInfo<R> pass = this.layerPass;
        SubmitNodeCollector tasks = this.layerTasks;
        if (pass == null || tasks == null || renderType == null) {
            return;
        }
        Map<com.geckolib.constant.dataticket.DataTicket<?>, Object> data = pass.renderState().getDataMap();
        Object oldLight = data.put(DataTickets.PACKED_LIGHT, packedLight);
        Object oldOverlay = data.put(DataTickets.PACKED_OVERLAY, packedOverlay);
        Object oldColor = data.put(DataTickets.RENDER_COLOR, net.minecraft.util.ARGB.colorFromFloat(
                Mth.clamp(alpha, 0.0F, 1.0F), Mth.clamp(red, 0.0F, 1.0F), Mth.clamp(green, 0.0F, 1.0F),
                Mth.clamp(blue, 0.0F, 1.0F)));
        PoseStack passStack = pass.poseStack();
        passStack.pushPose();
        passStack.last().set(pass.getModelRenderMatrixPose());
        try {
            super.submitRenderTasks(pass, tasks.order(1), renderType);
        } finally {
            passStack.popPose();
            restore(data, DataTickets.PACKED_LIGHT, oldLight);
            restore(data, DataTickets.PACKED_OVERLAY, oldOverlay);
            restore(data, DataTickets.RENDER_COLOR, oldColor);
        }
    }

    private static void restore(Map<com.geckolib.constant.dataticket.DataTicket<?>, Object> data,
                                com.geckolib.constant.dataticket.DataTicket<?> ticket, @Nullable Object old) {
        if (old == null) {
            data.remove(ticket);
        } else {
            data.put(ticket, old);
        }
    }

    // ---- GeckoLib overrides ----

    /**
     * Applies each controller's keyframes in order, and the flight blend right after the flight controller,
     * so controllers registered later still override the blended flight pose as they did on 1.20.1.
     */
    @Override
    public void applyAnimationControllers(RenderPassInfo<R> renderPassInfo, BoneSnapshots boneSnapshots) {
        com.geckolib.animation.state.ControllerState[] controllerStates = renderPassInfo.getOrDefaultGeckolibData(
                DataTickets.ANIMATION_CONTROLLER_STATES, new com.geckolib.animation.state.ControllerState[0]);
        com.leon.saintsdragons.util.animation.DragonFlightAnimationController.FlightFrame flightFrame =
                renderPassInfo.renderState().getGeckolibData(
                        com.leon.saintsdragons.util.animation.DragonFlightAnimationController.FRAME);
        try {
            for (com.geckolib.animation.state.ControllerState controllerState : controllerStates) {
                com.geckolib.animation.AnimationProcessor.createBoneSnapshots(controllerState, boneSnapshots);
                if (flightFrame != null && flightFrame.state() == controllerState) {
                    flightFrame.apply(boneSnapshots);
                }
            }
        } catch (Exception exception) {
            com.geckolib.GeckoLibConstants.LOGGER.error("Error while applying animations for a Saint's Dragons entity",
                    exception);
        }
    }

    @Override
    public void captureDefaultRenderState(T animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        super.captureDefaultRenderState(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(SdTickets.ENTITY, animatable);
        renderState.addGeckolibData(SdTickets.MODEL_SCALE, getRenderScale(animatable));
        renderState.addGeckolibData(SdTickets.MODEL_DATA, modelData(animatable, partialTick));
        this.shadowRadius = getShadowRadius(animatable);
    }

    private static EntityModelData modelData(Entity entity, float partialTick) {
        if (!(entity instanceof LivingEntity living)) {
            return new EntityModelData(false, false, 0.0F, -entity.getXRot(partialTick));
        }
        boolean sitting = living.isPassenger();
        float bodyYaw = Mth.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, living.yHeadRotO, living.yHeadRot);
        float netHeadYaw = headYaw - bodyYaw;
        if (sitting && living.getVehicle() instanceof LivingEntity vehicle) {
            bodyYaw = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
            netHeadYaw = Mth.clamp(Mth.wrapDegrees(headYaw - bodyYaw), -85.0F, 85.0F);
        }
        float headPitch = Mth.lerp(partialTick, living.xRotO, living.getXRot());
        return new EntityModelData(sitting, living.isBaby(), -netHeadYaw, -headPitch);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        if (!(getGeoModel() instanceof PosedGeoModel<?> posed)) {
            return;
        }
        R state = renderPassInfo.renderState();
        T entity = entityOf(state);
        if (entity == null) {
            return;
        }
        EntityModelData data = state.getOrDefaultGeckolibData(SdTickets.MODEL_DATA,
                new EntityModelData(false, false, 0.0F, 0.0F));
        float partialTick = state.getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 1.0F);
        ((PosedGeoModel<T>) posed).pose(entity, entity.getId(), new PoseState<>(entity, partialTick, data),
                renderPassInfo.model(), snapshots);
    }

    @Override
    public void preRenderPass(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        boneMatrices.clear();
        bonesCaptured = false;
        passModel = renderPassInfo.model();
        CameraRenderState camera = renderPassInfo.cameraState();
        // No world positions from a shader shadow pass: its matrices are relative to the sun, not the camera.
        passCameraPos = camera == null || ShaderPass.isShadowPass() ? null : camera.pos;
        R state = renderPassInfo.renderState();
        T entity = entityOf(state);
        passRecorder = null;
        if (entity != null) {
            float partialTick = state.getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 1.0F);
            passRecorder = new RecordingBufferSource();
            render(entity, entity.getYRot(partialTick), partialTick, renderPassInfo.poseStack(), passRecorder,
                    renderPassInfo.packedLight());
            preRender(renderPassInfo.poseStack(), entity, renderPassInfo.model(), passRecorder, null, false,
                    partialTick, renderPassInfo.packedLight(), renderPassInfo.packedOverlay(), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo, float widthScale, float heightScale) {
        Float modelScale = renderPassInfo.renderState().getOrDefaultGeckolibData(SdTickets.MODEL_SCALE, 1.0F);
        float factor = modelScale == null ? 1.0F : modelScale;
        super.scaleModelForRender(renderPassInfo, widthScale * factor, heightScale * factor);
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<R> renderPassInfo, OrderedSubmitNodeCollector renderTasks,
                                  @Nullable RenderType renderType) {
        if (!bonesCaptured) {
            // Every bone updater has been added by now, so the pose of this pass is final.
            bonesCaptured = true;
            captureBoneMatrices(renderPassInfo);
        }
        T entity = entityOf(renderPassInfo.renderState());
        if (renderType == null || entity == null || renderPassInfo.model().isMissingno()) {
            super.submitRenderTasks(renderPassInfo, renderTasks, renderType);
            return;
        }
        final float partialTick = renderPassInfo.renderState().getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 1.0F);
        final int packedLight = modelLight(entity, renderPassInfo.packedLight());
        final int packedOverlay = renderPassInfo.packedOverlay();
        final int renderColor = modelColor(entity, renderPassInfo.renderColor(), partialTick);
        if (!shouldSubmitModel(entity, partialTick)) {
            return;
        }
        renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
            final PoseStack poseStack = renderPassInfo.poseStack();
            poseStack.pushPose();
            poseStack.last().set(pose);
            final com.mojang.blaze3d.vertex.VertexConsumer wrapped = wrapModelBuffer(entity, vertexConsumer, partialTick);
            renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, wrapped, packedLight,
                    packedOverlay, renderColor));
            poseStack.popPose();
        });
    }

    /** Lets a renderer filter the vertices of the model itself (for example to scroll its texture). */
    protected com.mojang.blaze3d.vertex.VertexConsumer wrapModelBuffer(T entity,
            com.mojang.blaze3d.vertex.VertexConsumer buffer, float partialTick) {
        return buffer;
    }

    /** The light the model is drawn with; override for full-bright models. */
    protected int modelLight(T entity, int packedLight) {
        return packedLight;
    }

    private @Nullable RenderPassInfo<R> rotationPass;
    private float rotationScale = 1.0F;

    @Override
    protected void applyRotations(RenderPassInfo<R> renderPassInfo, PoseStack poseStack, float nativeScale) {
        T entity = entityOf(renderPassInfo.renderState());
        if (entity == null) {
            super.applyRotations(renderPassInfo, poseStack, nativeScale);
            return;
        }
        float partialTick = renderPassInfo.renderState().getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 1.0F);
        rotationPass = renderPassInfo;
        rotationScale = nativeScale;
        try {
            applyRotations(entity, poseStack, entity.tickCount + partialTick,
                    renderPassInfo.renderState().getOrDefaultGeckolibData(DataTickets.ENTITY_BODY_YAW, 0.0F), partialTick);
        } finally {
            rotationPass = null;
        }
    }

    /** 1.20.1 rotation hook; the base applies the standard GeckoLib entity rotations. */
    protected void applyRotations(T entity, PoseStack poseStack, float ageInTicks, float rotationYaw,
                                  float partialTick) {
        if (rotationPass != null) {
            super.applyRotations(rotationPass, poseStack, rotationScale);
        }
    }

    @Override
    public void postRenderPass(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.postRenderPass(renderPassInfo, renderTasks);
        R state = renderPassInfo.renderState();
        T entity = entityOf(state);
        if (entity != null && bonesCaptured) {
            afterModelSubmitted(entity, renderPassInfo, renderTasks,
                    state.getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 1.0F));
        }
        if (passRecorder != null) {
            passRecorder.flush(renderTasks);
            passRecorder = null;
        }
        boneMatrices.clear();
        passModel = null;
    }

    @Override
    public @Nullable RenderType getRenderType(R renderState, Identifier texture) {
        T entity = entityOf(renderState);
        if (entity == null) {
            return renderType.apply(texture);
        }
        if (renderState.isInvisible) {
            // Invisible (a cloaked Nulljaw, a potion): ghosted for spectators and team mates, hidden otherwise.
            if (!renderState.getOrDefaultGeckolibData(DataTickets.INVISIBLE_TO_PLAYER, false)) {
                return RenderTypes.entityTranslucentCullItemTarget(texture);
            }
            return renderState.appearsGlowing() ? RenderTypes.outline(texture) : null;
        }
        return getRenderType(entity, texture, null, renderState.getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 1.0F));
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        T entity = entityOf(renderState);
        return entity == null ? 90.0F : getDeathMaxRotation(entity);
    }

    @Override
    public Identifier getTextureLocation(R renderState) {
        T entity = entityOf(renderState);
        Identifier override = entity == null ? null : getTextureLocation(entity);
        return override != null ? override : super.getTextureLocation(renderState);
    }
}
