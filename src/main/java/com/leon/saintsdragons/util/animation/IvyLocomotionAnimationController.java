package com.leon.saintsdragons.util.animation;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.animation.state.ControllerState;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.loading.math.MolangQueries;
import com.geckolib.loading.math.value.Variable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.leon.saintsdragons.server.entity.npc.IvyFallAnimationState;
import com.leon.saintsdragons.server.entity.npc.IvyLocomotionBlend;
import com.leon.saintsdragons.server.entity.npc.IvyTheDragonMerchant;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Blends Ivy's idle, walk, run, fall and landing clips per bone.
 *
 * Like the flight controller, the clip times and weights are worked out while the render state is built and
 * the pose is written right after this controller's own keyframes during the render pass.
 */
public final class IvyLocomotionAnimationController extends EntityAnimationController<IvyTheDragonMerchant> {
    private static final String PREFIX = "ivy_oleander.animation.";
    private static final float ENTRY_TICKS = 3.0F;
    private static final double WALK_RIGHT_CONTACT_SECONDS = 0.00D;
    private static final double WALK_LEFT_CONTACT_SECONDS = 0.46D;
    private static final double RUN_RIGHT_CONTACT_SECONDS = 0.10D;
    private static final double RUN_LEFT_CONTACT_SECONDS = 0.43D;

    private Animation idle;
    private Animation walk;
    private Animation run;
    private Animation falling;
    private Animation landLight;
    private Animation landHeavy;
    private final Map<String, Integer> idleTracks = new HashMap<>();
    private final Map<String, Integer> walkTracks = new HashMap<>();
    private final Map<String, Integer> runTracks = new HashMap<>();
    private final Map<String, Integer> fallTracks = new HashMap<>();
    private final Map<String, Integer> lightLandingTracks = new HashMap<>();
    private final Map<String, Integer> heavyLandingTracks = new HashMap<>();
    private final Set<String> gaitBones = new LinkedHashSet<>();
    private final Map<String, Integer> gaitChannels = new HashMap<>();
    private final Set<Variable> clipVariables = new ReferenceOpenHashSet<>();
    private final Map<String, float[]> entryPose = new HashMap<>();
    private final PoseBlending blending = new PoseBlending();
    private final float[] idlePose = new float[9];
    private final float[] walkPose = new float[9];
    private final float[] runPose = new float[9];
    private final float[] fallPose = new float[9];
    private final float[] lightLandingPose = new float[9];
    private final float[] heavyLandingPose = new float[9];
    private final float[] landingPose = new float[9];
    private final float[] mixedPose = new float[9];
    /** Times are in ticks, like the entity's animation age. */
    private double entryTime = Double.NaN;
    private double lastSeekTime = Double.NaN;
    private float entryDuration = ENTRY_TICKS;
    private long lastLandingTick = -1L;

    public IvyLocomotionAnimationController(IvyTheDragonMerchant ivy,
                                           AnimationStateHandler<IvyTheDragonMerchant> predicate) {
        super(ivy, "movement", 3, predicate);
    }

    @Override
    public @Nullable ControllerState extractControllerState(IvyTheDragonMerchant animatable,
                                                           GeoRenderState renderState,
                                                           AnimatableManager<IvyTheDragonMerchant> manager,
                                                           MolangQueries.Actor<IvyTheDragonMerchant> actor,
                                                           GeoModel<IvyTheDragonMerchant> geoModel) {
        ControllerState state = super.extractControllerState(animatable, renderState, manager, actor, geoModel);
        if (state == null || !animatable.isLocomotionBlendActive() || isPlayingTriggeredAnimation()
                || !loadClips(animatable, geoModel)) {
            clearEntry();
            return state;
        }
        double seekTime = renderState.getAnimatableAge();
        IvyFallAnimationState air = animatable.getFallAnimationState();
        long landingTick = air.landingTick();
        boolean newLanding = air.isLanding() && landingTick != lastLandingTick;
        lastLandingTick = landingTick;
        boolean captureEntry = false;
        if (Double.isNaN(entryTime) || seekTime < lastSeekTime || seekTime - lastSeekTime > 10.0D) {
            entryTime = seekTime;
            entryDuration = newLanding ? 1.0F : ENTRY_TICKS;
            captureEntry = true;
        } else if (newLanding) {
            // Retain the outgoing pose even if a landing packet arrives partway through compression.
            entryTime = seekTime;
            entryDuration = 1.0F;
            captureEntry = true;
        }
        lastSeekTime = seekTime;
        float entryWeight = Mth.clamp((float) (seekTime - entryTime) / entryDuration, 0.0F, 1.0F);
        IvyLocomotionBlend blend = animatable.getLocomotionBlend();
        float partialTick = renderState.getPartialTick();
        float movingWeight = blend.moving(partialTick);
        float runningWeight = blend.running(partialTick);
        double phase = blend.phase(partialTick);
        double walkLength = ticks(walk);
        double runLength = ticks(run);
        double idleLength = ticks(idle);
        double walkTime = contactTick(phase, walkLength, WALK_RIGHT_CONTACT_SECONDS, WALK_LEFT_CONTACT_SECONDS);
        double runTime = contactTick(phase, runLength, RUN_RIGHT_CONTACT_SECONDS, RUN_LEFT_CONTACT_SECONDS);
        double idleTime = idleLength > 0.0D ? seekTime % idleLength : 0.0D;
        float fallWeight = falling != null ? air.falling(partialTick) : 0.0F;
        float landingWeight = landLight != null && landHeavy != null ? air.landingWeight(partialTick) : 0.0F;
        double fallTime = falling != null && ticks(falling) > 0.0D ? air.fallTime(partialTick) % ticks(falling) : 0.0D;
        double lightTime = landLight != null ? air.landingSourceTick(partialTick, false, ticks(landLight)) : 0.0D;
        double heavyTime = landHeavy != null ? air.landingSourceTick(partialTick, true, ticks(landHeavy)) : 0.0D;

        Reference2DoubleMap<Variable> queryValues = new Reference2DoubleOpenHashMap<>(state.queryValues());
        MolangQueries.buildActorVariables(actor, clipVariables, queryValues);
        final boolean capture = captureEntry;
        final float heavyShare = air.heavyShare();
        final float movingLegInfluence = air.movingLegInfluence();
        renderState.addGeckolibData(DragonFlightAnimationController.FRAME,
                new DragonFlightAnimationController.FlightFrame() {
                    @Override
                    public ControllerState state() {
                        return state;
                    }

                    @Override
                    public void apply(BoneSnapshots snapshots) {
                        applyBlend(snapshots, renderState, queryValues, idleTime / 20.0D, walkTime / 20.0D,
                                runTime / 20.0D, fallTime / 20.0D, lightTime / 20.0D, heavyTime / 20.0D,
                                movingWeight, runningWeight, fallWeight, landingWeight, heavyShare,
                                movingLegInfluence, entryWeight, capture);
                    }
                });
        return state;
    }

    private void applyBlend(BoneSnapshots snapshots, GeoRenderState renderState,
                            Reference2DoubleMap<Variable> queryValues, double idleTime, double walkTime,
                            double runTime, double fallTime, double lightTime, double heavyTime,
                            float movingWeight, float runningWeight, float fallWeight, float landingWeight,
                            float heavyShare, float movingLegInfluence, float entryWeight, boolean captureEntry) {
        PoseBlending.Sampler idleSampler = new PoseBlending.Sampler(idle, idleTime, renderState, queryValues);
        PoseBlending.Sampler walkSampler = new PoseBlending.Sampler(walk, walkTime, renderState, queryValues);
        PoseBlending.Sampler runSampler = new PoseBlending.Sampler(run, runTime, renderState, queryValues);
        PoseBlending.Sampler fallSampler = fallWeight > 0.0F
                ? new PoseBlending.Sampler(falling, fallTime, renderState, queryValues) : null;
        PoseBlending.Sampler lightSampler = landingWeight > 0.0F
                ? new PoseBlending.Sampler(landLight, lightTime, renderState, queryValues) : null;
        PoseBlending.Sampler heavySampler = landingWeight > 0.0F
                ? new PoseBlending.Sampler(landHeavy, heavyTime, renderState, queryValues) : null;
        if (captureEntry) {
            entryPose.clear();
        }
        for (String name : gaitBones) {
            BoneSnapshot snapshot = snapshots.get(name).orElse(null);
            if (snapshot == null) continue;
            GeoBone bone = snapshot.getBone();
            if (captureEntry) {
                entryPose.put(name, PoseBlending.capture(snapshot));
            }
            idleSampler.sample(snapshot, idleTracks.get(name), idlePose);
            walkSampler.sample(snapshot, walkTracks.get(name), walkPose);
            runSampler.sample(snapshot, runTracks.get(name), runPose);
            blending.mix(walkPose, runPose, runningWeight, mixedPose, bone);
            blending.mix(idlePose, mixedPose, movingWeight, mixedPose, bone);
            if (fallSampler != null) {
                fallSampler.sample(snapshot, fallTracks.get(name), fallPose);
                blending.mix(mixedPose, fallPose, fallWeight, mixedPose, bone);
            }
            if (lightSampler != null && heavySampler != null) {
                lightSampler.sample(snapshot, lightLandingTracks.get(name), lightLandingPose);
                heavySampler.sample(snapshot, heavyLandingTracks.get(name), heavyLandingPose);
                blending.mix(lightLandingPose, heavyLandingPose, heavyShare, landingPose, bone);
                // Let moving feet retain their stride, with more leg authority for a heavy impact.
                float legWeight = name.equals("leftleg") || name.equals("rightleg")
                        ? Mth.lerp(movingWeight, 1.0F, movingLegInfluence) : 1.0F;
                blending.mix(mixedPose, landingPose, landingWeight * legWeight, mixedPose, bone);
            }
            float[] entry = entryPose.get(name);
            if (entryWeight < 1.0F && entry != null) blending.mix(entry, mixedPose, entryWeight, mixedPose, bone);
            PoseBlending.writePose(snapshot, mixedPose, gaitChannels.getOrDefault(name, 0));
        }
    }

    private static double ticks(Animation animation) {
        return animation.length() * 20.0D;
    }

    private static @Nullable Animation find(GeoModel<IvyTheDragonMerchant> model, IvyTheDragonMerchant animatable,
                                            String name) {
        try {
            return model.getBakedAnimation(animatable, name);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private boolean loadClips(IvyTheDragonMerchant animatable, GeoModel<IvyTheDragonMerchant> model) {
        Animation nextIdle = find(model, animatable, PREFIX + "idle");
        Animation nextWalk = find(model, animatable, PREFIX + "walk");
        Animation nextRun = find(model, animatable, PREFIX + "run");
        Animation nextFall = find(model, animatable, PREFIX + "falling");
        Animation nextLight = find(model, animatable, PREFIX + "land_light");
        Animation nextHeavy = find(model, animatable, PREFIX + "land_heavy");
        if (nextIdle == null || nextWalk == null || nextRun == null) return false;
        if (idle != nextIdle || walk != nextWalk || run != nextRun
                || falling != nextFall || landLight != nextLight || landHeavy != nextHeavy) {
            idle = nextIdle;
            walk = nextWalk;
            run = nextRun;
            falling = nextFall;
            landLight = nextLight;
            landHeavy = nextHeavy;
            gaitBones.clear();
            gaitChannels.clear();
            clipVariables.clear();
            index(idle, idleTracks);
            index(walk, walkTracks);
            index(run, runTracks);
            index(falling, fallTracks);
            index(landLight, lightLandingTracks);
            index(landHeavy, heavyLandingTracks);
            clearEntry();
        }
        return true;
    }

    private void index(@Nullable Animation animation, Map<String, Integer> tracks) {
        tracks.clear();
        if (animation == null) return;
        clipVariables.addAll(animation.usedVariables());
        BoneAnimation[] boneAnimations = animation.boneAnimations();
        for (int i = 0; i < boneAnimations.length; i++) {
            BoneAnimation track = boneAnimations[i];
            tracks.put(track.boneName(), i);
            gaitBones.add(track.boneName());
            gaitChannels.merge(track.boneName(), PoseBlending.channels(track), (first, second) -> first | second);
        }
    }

    private void clearEntry() {
        entryTime = lastSeekTime = Double.NaN;
        entryPose.clear();
    }

    private static double contactTick(double phase, double length, double rightSeconds, double leftSeconds) {
        if (length <= 0.0D) return 0.0D;
        double rightTick = Mth.clamp(rightSeconds * 20.0D, 0.0D, length);
        double leftTick = Mth.clamp(leftSeconds * 20.0D, rightTick, length);
        double fraction = phase - Math.floor(phase);
        double firstStepTicks = leftTick - rightTick;
        double sourceTick = fraction < 0.5D
                ? rightTick + fraction * 2.0D * firstStepTicks
                : leftTick + (fraction - 0.5D) * 2.0D * (length - firstStepTicks);
        return sourceTick % length;
    }
}
