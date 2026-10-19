package com.leon.saintsdragons.util.animation;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.animation.state.ControllerState;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.loading.math.MathParser;
import com.geckolib.loading.math.MolangQueries;
import com.geckolib.loading.math.value.Variable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.leon.saintsdragons.server.entity.base.RideableFlyingDragon;
import com.leon.saintsdragons.server.flight.DragonFlightAnimationProfile;
import com.leon.saintsdragons.server.flight.DragonFlightBlend;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Blends only the flight layer, before the model's procedural neck, tail and dive offsets.
 *
 * GeckoLib 5 evaluates controllers in two steps: the controller state is extracted while the render state is
 * built, and bones are posed later from that state. The blend follows the same split: {@link #extractControllerState}
 * works out the clip times and weights for the frame, and {@link Frame#apply} overwrites this controller's
 * channels right after its own keyframes have been applied (the renderer calls it in controller order).
 */
public final class DragonFlightAnimationController<T extends RideableFlyingDragon> extends EntityAnimationController<T> {
    public static final DataTicket<FlightFrame> FRAME =
            DataTicket.create("saintsdragons_flight_blend", FlightFrame.class);

    /** The flight pose of one frame, applied by the renderer after this controller keyframes. */
    public interface FlightFrame {
        ControllerState state();

        void apply(BoneSnapshots snapshots);
    }

    private static final int ROTATION = 1;
    private static final int POSITION = 2;
    private static final int SCALE = 4;
    private Animation glide;
    private Animation flap;
    private Animation sprint;
    private Animation idle;
    private DragonFlightAnimationProfile profile;
    private final Map<String, Integer> glideTracks = new HashMap<>();
    private final Map<String, Integer> flapTracks = new HashMap<>();
    private final Map<String, Integer> sprintTracks = new HashMap<>();
    private final Map<String, Integer> idleTracks = new HashMap<>();
    private final Set<String> flightBones = new LinkedHashSet<>();
    private final Map<String, Integer> flightChannels = new HashMap<>();
    private final Set<Variable> clipVariables = new ReferenceOpenHashSet<>();
    private final Map<String, float[]> entryPose = new HashMap<>();
    private final float[] glidePose = new float[9];
    private final float[] flapPose = new float[9];
    private final float[] sprintPose = new float[9];
    private final float[] idlePose = new float[9];
    private final float[] mixedPose = new float[9];
    private final Quaternionf rotation = new Quaternionf();
    private final Quaternionf otherRotation = new Quaternionf();
    private final Vector3f angles = new Vector3f();
    /** Times are in ticks, like the entity's animation age. */
    private double entryTime = Double.NaN;
    private double lastSeekTime = Double.NaN;
    private double lastSoundPhase = Double.NaN;
    private long lastSoundCycle = Long.MIN_VALUE;
    private double flapSoundPhase;
    private double sprintSoundPhase;
    private double idleSoundPhase;

    public DragonFlightAnimationController(T dragon, int transitionTicks,
                                          AnimationStateHandler<T> predicate) {
        super(dragon, AnimationHelper.FLIGHT_CONTROLLER, Math.max(0, transitionTicks), state -> {
            state.controller().setTransitionTicks(Math.max(0, transitionTicks));
            return predicate.handle(state);
        });
    }

    /** Everything needed to pose the flight bones for one frame. */
    public final class Frame implements FlightFrame {
        private final ControllerState state;
        private final GeoRenderState renderState;
        private final Reference2DoubleMap<Variable> queryValues;
        private final double glideTime;
        private final double glideExpressionTime;
        private final double flapTime;
        private final double flapExpressionTime;
        private final double sprintTime;
        private final double sprintExpressionTime;
        private final double idleTime;
        private final double idleExpressionTime;
        private final float poweredWeight;
        private final float sprintShare;
        private final float idleWeight;
        private final float entryWeight;
        private final boolean captureEntry;

        private Frame(ControllerState state, GeoRenderState renderState, Reference2DoubleMap<Variable> queryValues,
                      double glideTime, double glideExpressionTime, double flapTime, double flapExpressionTime,
                      double sprintTime, double sprintExpressionTime, double idleTime, double idleExpressionTime,
                      float poweredWeight, float sprintShare, float idleWeight, float entryWeight,
                      boolean captureEntry) {
            this.state = state;
            this.renderState = renderState;
            this.queryValues = queryValues;
            this.glideTime = glideTime;
            this.glideExpressionTime = glideExpressionTime;
            this.flapTime = flapTime;
            this.flapExpressionTime = flapExpressionTime;
            this.sprintTime = sprintTime;
            this.sprintExpressionTime = sprintExpressionTime;
            this.idleTime = idleTime;
            this.idleExpressionTime = idleExpressionTime;
            this.poweredWeight = poweredWeight;
            this.sprintShare = sprintShare;
            this.idleWeight = idleWeight;
            this.entryWeight = entryWeight;
            this.captureEntry = captureEntry;
        }

        /** The controller state this frame belongs to; the blend is applied right after it. */
        @Override
        public ControllerState state() {
            return state;
        }

        /** Replaces this controller's channels without taking ownership of unauthored channels. */
        @Override
        public void apply(BoneSnapshots snapshots) {
            Sampler glideSampler = new Sampler(glide, glideTime, glideExpressionTime);
            Sampler flapSampler = new Sampler(flap, flapTime, flapExpressionTime);
            Sampler sprintSampler = new Sampler(sprint, sprintTime, sprintExpressionTime);
            Sampler idleSampler = new Sampler(idle, idleTime, idleExpressionTime);
            if (captureEntry) {
                entryPose.clear();
            }
            for (String name : flightBones) {
                BoneSnapshot snapshot = snapshots.get(name).orElse(null);
                if (snapshot == null) continue;
                GeoBone bone = snapshot.getBone();
                if (captureEntry) {
                    entryPose.put(name, new float[] {
                            snapshot.getRotX(), snapshot.getRotY(), snapshot.getRotZ(),
                            snapshot.getTranslateX(), snapshot.getTranslateY(), snapshot.getTranslateZ(),
                            snapshot.getScaleX(), snapshot.getScaleY(), snapshot.getScaleZ()});
                }
                glideSampler.sample(snapshot, glideTracks.get(name), glidePose);
                flapSampler.sample(snapshot, flapTracks.get(name), flapPose);
                sprintSampler.sample(snapshot, sprintTracks.get(name), sprintPose);
                idleSampler.sample(snapshot, idleTracks.get(name), idlePose);
                mix(flapPose, sprintPose, sprintShare, mixedPose, bone);
                mix(glidePose, mixedPose, poweredWeight, mixedPose, bone);
                mix(mixedPose, idlePose, idleWeight, mixedPose, bone);
                float[] entry = entryPose.get(name);
                if (entryWeight < 1.0F && entry != null) mix(entry, mixedPose, entryWeight, mixedPose, bone);
                writePose(snapshot, mixedPose, flightChannels.getOrDefault(name, 0));
            }
        }

        /** Evaluates one clip at a fixed time through GeckoLib's own keyframe evaluation. */
        private final class Sampler {
            private final AnimationPoint point;
            private final ControllerState sampleState;

            private Sampler(Animation animation, double time, double expressionTime) {
                this.point = AnimationPoint.createFor(animation, null, animation.loopType(), time);
                Reference2DoubleMap<Variable> values = new Reference2DoubleOpenHashMap<>(queryValues);
                // Keyed tracks can loop while procedural expressions keep their longer secondary motions continuous.
                values.put(MathParser.getVariableFor(MolangQueries.ANIM_TIME), expressionTime);
                this.sampleState = new ControllerState(point, null, -1, 0, false, null, renderState, values);
            }

            private void sample(BoneSnapshot snapshot, @Nullable Integer track, float[] pose) {
                pose[0] = pose[1] = pose[2] = pose[3] = pose[4] = pose[5] = 0.0F;
                pose[6] = pose[7] = pose[8] = 1.0F;
                if (track == null) return;
                sample(snapshot, track, AnimationPoint.Transform.ROTATION, pose, 0);
                sample(snapshot, track, AnimationPoint.Transform.TRANSLATION, pose, 3);
                sample(snapshot, track, AnimationPoint.Transform.SCALE, pose, 6);
            }

            private void sample(BoneSnapshot snapshot, int track, AnimationPoint.Transform transform, float[] pose,
                                int offset) {
                pose[offset] = AnimationProcessor.findAnimationPointValue(snapshot, sampleState, point, null, track,
                        transform, AnimationPoint.Axis.X, null);
                pose[offset + 1] = AnimationProcessor.findAnimationPointValue(snapshot, sampleState, point, null, track,
                        transform, AnimationPoint.Axis.Y, null);
                pose[offset + 2] = AnimationProcessor.findAnimationPointValue(snapshot, sampleState, point, null, track,
                        transform, AnimationPoint.Axis.Z, null);
            }
        }
    }

    @Override
    public @Nullable ControllerState extractControllerState(T animatable, GeoRenderState renderState,
                                                           AnimatableManager<T> manager,
                                                           MolangQueries.Actor<T> actor, GeoModel<T> geoModel) {
        ControllerState state = super.extractControllerState(animatable, renderState, manager, actor, geoModel);
        if (state == null || !prepareBlend(animatable, geoModel)) {
            if (state == null) {
                resetBlend();
            }
            return state;
        }
        double seekTime = renderState.getAnimatableAge();
        boolean captureEntry = false;
        if (Double.isNaN(entryTime) || seekTime < lastSeekTime || seekTime - lastSeekTime > 10.0D) {
            entryTime = seekTime;
            captureEntry = true;
        }
        lastSeekTime = seekTime;
        float entryWeight = profile.entryTicks() <= 0 ? 1.0F
                : Mth.clamp((float) ((seekTime - entryTime) / profile.entryTicks()), 0.0F, 1.0F);
        float partialTick = renderState.getPartialTick();
        DragonFlightBlend blend = animatable.getFlightBlend();
        float flapWeight = blend.flap(partialTick);
        float sprintWeight = blend.sprint(partialTick);
        float idleWeight = blend.idle(partialTick);
        float poweredWeight = Mth.clamp(flapWeight + sprintWeight, 0.0F, 1.0F);
        float sprintShare = poweredWeight > 0.0001F ? sprintWeight / poweredWeight : 0.0F;
        // Dive suppresses the tick-driven flap target, not the current rendered weight.
        // The model scales its existing dive offset by the same gradually increasing glide authority.
        double phase = blend.phase(partialTick);
        double glideLength = ticks(glide);
        double flapLength = ticks(flap);
        double sprintLength = ticks(sprint);
        double idleLength = ticks(idle);
        double flapSourceTime = profile.clips().flap().sourceTick(phase, flapLength);
        double sprintSourceTime = profile.clips().sprint().sourceTick(phase, sprintLength);
        double idleSourceTime = profile.clips().idle().sourceTick(phase, idleLength);
        double flapTime = loopTick(flapSourceTime, flapLength);
        double sprintTime = loopTick(sprintSourceTime, sprintLength);
        double idleTime = loopTick(idleSourceTime, idleLength);

        Reference2DoubleMap<Variable> queryValues = new Reference2DoubleOpenHashMap<>(state.queryValues());
        MolangQueries.buildActorVariables(actor, clipVariables, queryValues);
        renderState.addGeckolibData(FRAME, new Frame(state, renderState, queryValues,
                loopTick(seekTime, glideLength) / 20.0D, seekTime / 20.0D,
                flapTime / 20.0D, (profile.clips().flap().continuousMolang() ? flapSourceTime : flapTime) / 20.0D,
                sprintTime / 20.0D, (profile.clips().sprint().continuousMolang() ? sprintSourceTime : sprintTime) / 20.0D,
                idleTime / 20.0D, (profile.clips().idle().continuousMolang() ? idleSourceTime : idleTime) / 20.0D,
                poweredWeight, sprintShare, idleWeight, entryWeight, captureEntry));
        playBlendedWingbeat(animatable, partialTick, entryWeight);
        return state;
    }

    private static double ticks(Animation animation) {
        return animation.length() * 20.0D;
    }

    private void resetBlend() {
        entryTime = lastSeekTime = lastSoundPhase = Double.NaN;
        lastSoundCycle = Long.MIN_VALUE;
        entryPose.clear();
    }

    private boolean prepareBlend(T animatable, GeoModel<T> model) {
        DragonFlightAnimationProfile nextProfile = animatable.getFlightAnimationProfile();
        if (nextProfile != profile) {
            profile = nextProfile;
            glide = null; // Rebuild clip timing too, even when the animation resources are unchanged.
            entryTime = Double.NaN;
        }
        if (profile == null || !animatable.isFlightBlendActive() || isPlayingTriggeredAnimation()
                || !loadClips(animatable, model)) {
            resetBlend();
            return false;
        }
        return true;
    }

    private void playBlendedWingbeat(T animatable, float partialTick, float entryWeight) {
        DragonFlightBlend blend = animatable.getFlightBlend();
        float flapWeight = blend.flap(partialTick);
        float sprintWeight = blend.sprint(partialTick);
        float idleWeight = blend.idle(partialTick);
        float poweredWeight = Mth.clamp(flapWeight + sprintWeight, 0.0F, 1.0F);
        float sprintShare = poweredWeight > 0.0001F ? sprintWeight / poweredWeight : 0.0F;
        double phase = blend.phase(partialTick);
        float audibleWeight = poweredWeight * (1.0F - idleWeight) + idleWeight;
        float idleSoundShare = audibleWeight > 0.0001F ? idleWeight / audibleWeight : 0.0F;
        playWingbeat(animatable, phase, audibleWeight * entryWeight, sprintShare, idleSoundShare);
    }

    private static <A extends RideableFlyingDragon> @Nullable Animation find(GeoModel<A> model, A animatable,
                                                                             String name) {
        try {
            return model.getBakedAnimation(animatable, name);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private boolean loadClips(T animatable, GeoModel<T> model) {
        Animation nextGlide = find(model, animatable, profile.clips().glide());
        Animation nextFlap = find(model, animatable, profile.clips().flap().name());
        Animation nextSprint = find(model, animatable, profile.clips().sprint().name());
        Animation nextIdle = find(model, animatable, profile.clips().idle().name());
        if (nextGlide == null || nextFlap == null || nextSprint == null || nextIdle == null) return false;
        if (nextGlide != glide || nextFlap != flap || nextSprint != sprint || nextIdle != idle) {
            glide = nextGlide;
            flap = nextFlap;
            sprint = nextSprint;
            idle = nextIdle;
            flightBones.clear();
            flightChannels.clear();
            clipVariables.clear();
            index(glide, glideTracks);
            index(flap, flapTracks);
            index(sprint, sprintTracks);
            index(idle, idleTracks);
            flapSoundPhase = soundPhase(flap, profile.clips().flap(), profile.clips().wingbeatSound());
            sprintSoundPhase = soundPhase(sprint, profile.clips().sprint(), profile.clips().wingbeatSound());
            idleSoundPhase = soundPhase(idle, profile.clips().idle(), profile.clips().wingbeatSound());
            entryTime = Double.NaN;
            lastSoundPhase = Double.NaN;
            lastSoundCycle = Long.MIN_VALUE;
        }
        return true;
    }

    private void index(Animation animation, Map<String, Integer> tracks) {
        tracks.clear();
        clipVariables.addAll(animation.usedVariables());
        BoneAnimation[] boneAnimations = animation.boneAnimations();
        for (int i = 0; i < boneAnimations.length; i++) {
            BoneAnimation track = boneAnimations[i];
            tracks.put(track.boneName(), i);
            flightBones.add(track.boneName());
            int channels = (track.rotationKeyFrames().xKeyframes().length == 0 ? 0 : ROTATION)
                    | (track.positionKeyFrames().xKeyframes().length == 0 ? 0 : POSITION)
                    | (track.scaleKeyFrames().xKeyframes().length == 0 ? 0 : SCALE);
            flightChannels.merge(track.boneName(), channels, (first, second) -> first | second);
        }
    }

    /** Poses hold rotation offsets from the rest pose; the rest rotation is added for the slerp. */
    private void mix(float[] from, float[] to, float weight, float[] result, GeoBone bone) {
        if (weight <= 0.0F || weight >= 1.0F) {
            System.arraycopy(weight <= 0.0F ? from : to, 0, result, 0, result.length);
            return;
        }
        // Z/Y/X matches GeckoLib's bone transform order. Slerp avoids Euler wrap and twisting artifacts.
        rotation.rotationZYX(from[2] + bone.baseRotZ(), from[1] + bone.baseRotY(), from[0] + bone.baseRotX());
        otherRotation.rotationZYX(to[2] + bone.baseRotZ(), to[1] + bone.baseRotY(), to[0] + bone.baseRotX());
        rotation.slerp(otherRotation, weight).getEulerAnglesZYX(angles);
        result[0] = angles.x - bone.baseRotX();
        result[1] = angles.y - bone.baseRotY();
        result[2] = angles.z - bone.baseRotZ();
        for (int i = 3; i < result.length; i++) result[i] = Mth.lerp(weight, from[i], to[i]);
    }

    private static void writePose(BoneSnapshot snapshot, float[] pose, int channels) {
        if ((channels & ROTATION) != 0) {
            snapshot.setRotation(pose[0], pose[1], pose[2]);
        }
        if ((channels & POSITION) != 0) {
            snapshot.setTranslation(pose[3], pose[4], pose[5]);
        }
        if ((channels & SCALE) != 0) {
            snapshot.setScale(pose[6], pose[7], pose[8]);
        }
    }

    private void playWingbeat(T animatable, double phase, float weight, float sprintShare, float idleShare) {
        double eventPhase = Mth.lerp(idleShare,
                Mth.lerp(sprintShare, flapSoundPhase, sprintSoundPhase), idleSoundPhase);
        long cycle = (long) Math.floor(phase - eventPhase);
        if (!Double.isNaN(lastSoundPhase) && phase >= lastSoundPhase && phase - lastSoundPhase < 0.5D
                && cycle > lastSoundCycle) {
            if (weight >= profile.minimumAudibleWingbeatWeight()) {
                animatable.playAnimationKeyframeSound(profile.clips().wingbeatSound());
            }
        }
        // Changing sprint or idle weights moves the sound marker; never emit twice for the same stroke.
        lastSoundCycle = Math.max(lastSoundCycle, cycle);
        lastSoundPhase = phase;
    }

    private static double loopTick(double tick, double length) {
        return length > 0.0D ? tick % length : tick;
    }

    private static double soundPhase(Animation animation, DragonFlightAnimationProfile.Clip clip, String soundKey) {
        double firstPhase = Double.NaN;
        double x = 0.0D;
        double y = 0.0D;
        // Long procedural clips contain several hand-placed flap effects. Average their stroke phases
        // around the circle so small placement differences (including across phase zero) do not bias timing.
        for (var sound : animation.keyframeMarkers().sounds()) {
            // Species sound profiles also accept numbered flap variants (for example raevyx_flap1).
            if (!sound.getSound().startsWith(soundKey)) continue;
            double phase = clip.soundPhase(sound.getTime() * 20.0D, ticks(animation));
            if (Double.isNaN(firstPhase)) firstPhase = phase;
            x += Math.cos(phase * Math.PI * 2.0D);
            y += Math.sin(phase * Math.PI * 2.0D);
        }
        if (Double.isNaN(firstPhase)) return 0.1D;
        if (x * x + y * y < 1.0E-8D) return firstPhase;
        double mean = Math.atan2(y, x) / (Math.PI * 2.0D);
        return mean - Math.floor(mean);
    }
}
