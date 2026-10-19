package com.leon.saintsdragons.util.animation;

import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.leon.saintsdragons.server.entity.base.RideableDragonBase;
import com.leon.saintsdragons.server.entity.base.RideableFlyingDragon;
import com.leon.saintsdragons.server.entity.dragons.ignivorus.Ignivorus;
import com.leon.saintsdragons.server.entity.interfaces.DancingEntity;
import com.leon.saintsdragons.server.flight.DragonFlightStateEvaluator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;

import java.util.function.Function;

public final class AnimationHelper {
    public static final String MOVEMENT_CONTROLLER = "movement";
    public static final String FLIGHT_CONTROLLER = "flight";
    public static final String INTERACTION_CONTROLLER = "interaction";
    public static final String VOCAL_CONTROLLER = "vocal";
    public static final String SIT_DOWN = "sit_down";
    public static final String SIT_UP = "sit_up";
    public static final String FALL_ASLEEP = "fall_asleep";
    public static final String SLEEP = "sleep";
    public static final String WAKE_UP = "wake_up";
    public static final String TAKEOFF = "takeoff";
    public static final String RIDER_TAKEOFF = "rider_takeoff";
    public static final String LANDED = "landed";
    public static final String PHASE2_TAKEOFF = "phase2_takeoff";
    public static final String PHASE2_LANDED = "phase2_landed";
    public static final String EAT = "eat";
    public static final String DIE = "die";
    public static final String DANCE = "dance";

    private AnimationHelper() {
    }

    /** Preserve the supplied layer order while installing shared sound and vocal handling. */
    @SafeVarargs
    public static <T extends DragonEntity> void registerDragonControllers(T dragon,
            AnimatableManager.ControllerRegistrar registrar, AnimationController<T> vocal,
            AnimationController<T>... orderedControllers) {
        registerSoundKeyframes(dragon, orderedControllers);
        registerGrumbles(vocal, dragon);
        registrar.add(orderedControllers);
    }

    /** Standard flight predicate; special triggered actions are handled by the caller first. */
    public static <T extends RideableFlyingDragon> PlayState handleFlightLocomotion(AnimationTest<T> state,
            FlightAnimations animations, FlightTransitions transitions) {
        return handleFlightLocomotion(state, animations, transitions, false, false,
                current -> current.animatable().evaluateMotionFlightState(
                        current.animatable().getFlightPitchRadians(current.getDataOrDefault(com.geckolib.constant.DataTickets.PARTIAL_TICK, 1.0F))));
    }

    public static <T extends RideableFlyingDragon> PlayState handleFlightLocomotion(AnimationTest<T> state,
            FlightAnimations animations, FlightTransitions transitions, boolean blocked, boolean riderTakeoff,
            Function<AnimationTest<T>, DragonFlightStateEvaluator.VisualState> resolvePose) {
        T dragon = state.animatable();
        if (blocked || dragon.isDying()
                || !(dragon.isFlying() || dragon.isTakeoff() || dragon.isLanding() || dragon.isHovering())) {
            return PlayState.STOP;
        }
        if (dragon.isTakeoff()) {
            return handleTakeoff(state, riderTakeoff, animations, transitions);
        }
        var pose = dragon.isFlightBlendActive()
                ? DragonFlightStateEvaluator.VisualState.GLIDE : resolvePose.apply(state);
        // Blended fliers apply their dive pose procedurally over the glide base.
        if (pose == DragonFlightStateEvaluator.VisualState.GLIDE_DOWN && dragon.getFlightAnimationProfile() != null) {
            pose = DragonFlightStateEvaluator.VisualState.GLIDE;
        }
        return handleFlightState(state, pose, animations, transitions);
    }

    @SafeVarargs
    public static <T extends DragonEntity> void registerSoundKeyframes(T dragon, AnimationController<T>... controllers) {
        for (AnimationController<T> controller : controllers) {
            controller.setSoundKeyframeHandler(event -> dragon.playAnimationKeyframeSound(event.keyframeData().getSound()));
        }
    }

    @SafeVarargs
    public static <T extends GeoAnimatable> void registerStepKeyframes(Entity entity, AnimationController<T>... controllers) {
        Identifier entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityTypeId == null) {
            return;
        }

        for (AnimationController<T> controller : controllers) {
            controller.setSoundKeyframeHandler(event -> {
                String soundKey = event.keyframeData().getSound();
                Identifier soundId = "step".equals(soundKey)
                        ? Identifier.fromNamespaceAndPath(entityTypeId.getNamespace(), entityTypeId.getPath() + "_step")
                        : soundKey != null && soundKey.endsWith("_step")
                                ? Identifier.fromNamespaceAndPath(entityTypeId.getNamespace(), soundKey)
                                : null;
                SoundEvent sound = soundId != null ? BuiltInRegistries.SOUND_EVENT.getValue(soundId) : null;
                if (sound != null) {
                    entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), sound,
                            SoundSource.NEUTRAL, 1.0f, 1.0f, false);
                }
            });
        }
    }

    public static <T extends RideableDragonBase> void registerRestAnimations(AnimationController<T> controller, Animations animations) {
        register(controller, SLEEP, animations.sleep());
        register(controller, SIT_DOWN, animations.sitDown());
        register(controller, SIT_UP, animations.sitUp());
        register(controller, FALL_ASLEEP, animations.fallAsleep());
        register(controller, WAKE_UP, animations.wakeUp());
    }

    public static void triggerRestAnimation(RideableDragonBase dragon, String animation) {
        if (dragon instanceof Ignivorus ignivorus) {
            ignivorus.triggerHitboxAnimation(MOVEMENT_CONTROLLER, animation);
        } else {
            dragon.triggerAnim(MOVEMENT_CONTROLLER, animation);
        }
    }

    public static PlayState idle(AnimationTest<?> state) {
        state.controller().setTransitionTicks(1);
        return PlayState.STOP;
    }

    public static <T extends DragonEntity> PlayState vocalIdle(AnimationTest<T> state) {
        state.controller().setTransitionTicks(2);
        return PlayState.STOP;
    }

    public static PlayState interactionIdle(AnimationTest<?> state) {
        state.controller().setTransitionTicks(1);
        return PlayState.STOP;
    }


    public static <T extends RideableDragonBase> PlayState handleGrounded(AnimationTest<T> state,
                                                                          T dragon,
                                                                          Animations animations,
                                                                          Transitions transitions,
                                                                          SpecialStates<T> specialStates) {
        var controller = state.controller();
        boolean aerialState = dragon.isFlying() || dragon.isTakeoff() || dragon.isLanding() || dragon.isHovering();

        if (dragon.isDying()) {
            return PlayState.STOP;
        }

        if (specialStates.tamingStunned(dragon)) {
            controller.setTransitionTicks(transitions.idle());
            setAndContinue(state, animations.stunned());
            return PlayState.CONTINUE;
        }

        if (specialStates.riderControlsLocked(dragon)) {
            return PlayState.STOP;
        }

        if (aerialState) {
            return PlayState.STOP;
        }

        if (specialStates.inWater(dragon)) {
            controller.setTransitionTicks(transitions.water());
            setAndContinue(state, animations.swim());
            return PlayState.CONTINUE;
        }

        if (dragon.isSleepTransitioning()) {
            return PlayState.STOP;
        }

        if (dragon.isSleeping()) {
            controller.setTransitionTicks(transitions.sleep());
            setAndContinue(state, animations.sleep());
            return PlayState.CONTINUE;
        }

        float sitProgress = dragon.getSitProgress();
        if (sitProgress >= dragon.maxSitTicks()) {
            controller.setTransitionTicks(transitions.sit());
            setAndContinue(state, animations.sit());
            return PlayState.CONTINUE;
        }
        if (sitProgress > 0f) {
            return PlayState.STOP;
        }

        PlayState dance = tryHandleDance(state, dragon, transitions.idle());
        if (dance != null) {
            return dance;
        }

        PlayState special = specialStates.handle(state, dragon, animations, transitions);
        if (special != null) {
            return special;
        }

        if (specialStates.falling(dragon)) {
            controller.setTransitionTicks(transitions.falling());
            setAndContinue(state, animations.falling());
            return PlayState.CONTINUE;
        }

        return handleGroundMovement(
                state,
                dragon,
                animations.idle(),
                animations.walk(),
                animations.run(),
                transitions.moving(),
                transitions.idle()
        );
    }

    public static <T extends RideableDragonBase> void register(AnimationController<T> controller, String trigger, RawAnimation animation) {
        if (animation != null) {
            controller.triggerableAnim(trigger, animation);
        }
    }

    public static boolean holdTriggeredAnimation(AnimationTest<?> state,
                                                 int transitionTicks,
                                                 RawAnimation... acceleratedAnimations) {
        RawAnimation triggeredAnimation = state.controller().isPlayingTriggeredAnimation() ? state.controller().getCurrentRawAnimation() : null;
        if (triggeredAnimation == null) {
            return false;
        }

        for (RawAnimation animation : acceleratedAnimations) {
            if (triggeredAnimation.equals(animation)) {
                state.controller().setTransitionTicks(Math.max(0, transitionTicks));
                break;
            }
        }
        return true;
    }

    public static <T extends DragonEntity> void registerFlight(AnimationController<T> controller,
                                                              String trigger,
                                                              RawAnimation animation) {
        if (trigger != null && !trigger.isEmpty() && animation != null) {
            controller.triggerableAnim(trigger, animation);
        }
    }

    public static <T extends DragonEntity> void registerFlightStandard(AnimationController<T> controller,
                                                                       RawAnimation takeoff,
                                                                       RawAnimation riderTakeoff,
                                                                       RawAnimation landed) {
        registerFlight(controller, TAKEOFF, takeoff);
        registerFlight(controller, RIDER_TAKEOFF, riderTakeoff);
        registerFlight(controller, LANDED, landed);
    }


    public static <T extends DragonEntity> AnimationController<T> createFlightController(T dragon,
                                                                                        int transitionTicks,
                                                                                        Function<AnimationTest<T>, PlayState> predicate) {
        int safeTransitionTicks = Math.max(0, transitionTicks);
        return new EntityAnimationController<>(dragon, FLIGHT_CONTROLLER, safeTransitionTicks, state -> {
            state.controller().setTransitionTicks(safeTransitionTicks);
            return predicate.apply(state);
        });
    }


    public static <T extends DragonEntity> PlayState handleFlightState(AnimationTest<T> state,
                                                                       DragonFlightStateEvaluator.VisualState visualState,
                                                                       FlightAnimations animations,
                                                                       FlightTransitions transitions) {
        if (visualState == null) {
            return PlayState.STOP;
        }

        RawAnimation animation = switch (visualState) {
            case TAKEOFF -> animations.takeoff();
            case LANDING -> animations.landing();
            case GLIDE_DOWN -> animations.glideDown();
            case FLY_IDLE -> animations.flyIdle();
            case SPRINT_FLAP -> animations.sprintFlap();
            case FLAP -> animations.flap();
            case GLIDE -> animations.flyGlide();
            default -> null;
        };

        if (animation == null) {
            return PlayState.STOP;
        }

        state.controller().setTransitionTicks(getFlightTransitionTicks(visualState, transitions));
        setAndContinue(state, animation);
        return PlayState.CONTINUE;
    }

    public static <T extends DragonEntity> PlayState handleTakeoff(AnimationTest<T> state,
                                                                   boolean riderTakeoff,
                                                                   FlightAnimations animations,
                                                                   FlightTransitions transitions) {
        RawAnimation animation = riderTakeoff && animations.riderTakeoff() != null
                ? animations.riderTakeoff()
                : animations.takeoff();
        if (animation == null) {
            return PlayState.STOP;
        }
        state.controller().setTransitionTicks(transitions.takeoff());
        setAndContinue(state, animation);
        return PlayState.CONTINUE;
    }



    private static int getFlightTransitionTicks(DragonFlightStateEvaluator.VisualState visualState, FlightTransitions transitions) {
        return switch (visualState) {
            case TAKEOFF -> transitions.takeoff();
            case LANDING -> transitions.landing();
            case GLIDE_DOWN -> transitions.glideDown();
            case FLY_IDLE -> transitions.flyIdle();
            case SPRINT_FLAP -> transitions.sprintFlap();
            case FLAP -> transitions.flap();
            case GLIDE -> transitions.flyGlide();
            default -> 0;
        };
    }

    public static <T extends DragonEntity> void registerGrumbles(AnimationController<T> controller, T dragon) {
        dragon.getVocalEntries().forEach((key, entry) -> {
            if (!isGrumbleKey(key) || entry.animationId() == null || entry.animationId().isEmpty()) {
                return;
            }
            controller.triggerableAnim(key, RawAnimation.begin().thenPlay(entry.animationId()));
        });
    }

    private static boolean isGrumbleKey(String key) {
        return key != null && key.contains("grumble");
    }

    public static void setAndContinue(AnimationTest<?> state, RawAnimation animation) {
        state.setAnimation(animation);
        if (animation != null
                && state.controller().getCurrentAnimationPoint() == null
                && state.isCurrentAnimation(animation)) {
            state.controller().reset();
            state.setAnimation(animation);
        }
    }

    public static PlayState tryHandleRestPose(AnimationTest<?> state,
                                              DragonEntity dragon,
                                              RawAnimation sleepAnimation,
                                              RawAnimation sitAnimation,
                                              int sleepTransitionTicks,
                                              int sitTransitionTicks) {
        return tryHandleRestPose(state, dragon, sleepAnimation, sitAnimation, sleepTransitionTicks, sitTransitionTicks, true);
    }

    public static PlayState tryHandleRestPose(AnimationTest<?> state,
                                              DragonEntity dragon,
                                              RawAnimation sleepAnimation,
                                              RawAnimation sitAnimation,
                                              int sleepTransitionTicks,
                                              int sitTransitionTicks,
                                              boolean allowSitPose) {
        if (dragon.isSleeping() && !dragon.isSleepTransitioning()) {
            if (sleepAnimation == null) {
                return PlayState.STOP;
            }
            state.controller().setTransitionTicks(sleepTransitionTicks);
            setAndContinue(state, sleepAnimation);
            return PlayState.CONTINUE;
        }
        boolean triggeredClipPlaying = state.controller().isPlayingTriggeredAnimation();
        if (dragon.isSleepTransitioning()) {
            if (triggeredClipPlaying) {
                return PlayState.STOP;
            }
            // The one-shot fall-asleep / wake-up clip is over but the server has not finished the transition
            // (a slow or throttled server ticks behind the animation). Hold a resting pose instead of dropping
            // to the bind pose.
            RawAnimation held = dragon.isSleepingExiting() || sleepAnimation == null ? sitAnimation : sleepAnimation;
            if (held == null) {
                return PlayState.STOP;
            }
            state.controller().setTransitionTicks(sleepTransitionTicks);
            setAndContinue(state, held);
            return PlayState.CONTINUE;
        }

        float sitProgress = dragon.getSitProgress();
        if (!allowSitPose && sitProgress > 0f) {
            return null;
        }
        if (sitProgress >= dragon.maxSitTicks()) {
            if (sitAnimation == null) {
                return PlayState.STOP;
            }
            state.controller().setTransitionTicks(sitTransitionTicks);
            setAndContinue(state, sitAnimation);
            return PlayState.CONTINUE;
        }
        if (sitProgress > 0f) {
            if (triggeredClipPlaying) {
                return PlayState.STOP;
            }
            // Between standing and sitting with no one-shot clip left to show: while sitting down keep the sit
            // pose, while standing up let the normal idle/walk selection take over.
            if (dragon.isOrderedToSit() && sitAnimation != null) {
                state.controller().setTransitionTicks(sitTransitionTicks);
                setAndContinue(state, sitAnimation);
                return PlayState.CONTINUE;
            }
            return null;
        }

        return null;
    }

    public static PlayState tryHandleDance(AnimationTest<?> state, DancingEntity dancer, int transitionTicks) {
        if (!dancer.isDancing() || !dancer.canDance()) {
            return null;
        }
        RawAnimation animation = dancer.getDanceAnimation();
        if (animation == null) {
            return PlayState.STOP;
        }
        state.controller().setTransitionTicks(transitionTicks);
        setAndContinue(state, animation);
        return PlayState.CONTINUE;
    }

    public static PlayState handleGroundMovement(AnimationTest<?> state,
                                                 RideableDragonBase dragon,
                                                 RawAnimation idleAnimation,
                                                 RawAnimation walkAnimation,
                                                 RawAnimation runAnimation) {
        return handleGroundMovement(state, dragon, idleAnimation, walkAnimation, runAnimation, false);
    }

    public static PlayState handleGroundMovement(AnimationTest<?> state,
                                                 RideableDragonBase dragon,
                                                 RawAnimation idleAnimation,
                                                 RawAnimation walkAnimation,
                                                 RawAnimation runAnimation,
                                                 boolean treatAnimationStateMovingAsWalk) {
        int groundState = dragon.getEffectiveGroundState();
        if (dragon.isVehicle()) {
            if (groundState == 2 || dragon.isRunning()) {
                setAndContinue(state, runAnimation);
            } else if (groundState == 1 || dragon.isWalking() || (treatAnimationStateMovingAsWalk && state.isMoving())) {
                setAndContinue(state, walkAnimation);
            } else {
                setAndContinue(state, idleAnimation);
            }
            return PlayState.CONTINUE;
        }

        if (groundState == 2 || (state.isMoving() && dragon.shouldUseRunAnimation())) {
            setAndContinue(state, runAnimation);
        } else if (groundState == 1 || state.isMoving()) {
            setAndContinue(state, walkAnimation);
        } else {
            setAndContinue(state, idleAnimation);
        }
        return PlayState.CONTINUE;
    }

    public static PlayState handleGroundMovement(AnimationTest<?> state,
                                                 RideableDragonBase dragon,
                                                 RawAnimation idleAnimation,
                                                 RawAnimation walkAnimation,
                                                 RawAnimation runAnimation,
                                                 int movingTransitionTicks,
                                                 int idleTransitionTicks) {
        return handleGroundMovement(state, dragon, idleAnimation, walkAnimation, runAnimation,
                movingTransitionTicks, idleTransitionTicks, false);
    }

    public static PlayState handleGroundMovement(AnimationTest<?> state,
                                                 RideableDragonBase dragon,
                                                 RawAnimation idleAnimation,
                                                 RawAnimation walkAnimation,
                                                 RawAnimation runAnimation,
                                                 int movingTransitionTicks,
                                                 int idleTransitionTicks,
                                                 boolean treatAnimationStateMovingAsWalk) {
        int groundState = dragon.getEffectiveGroundState();
        if (dragon.isVehicle()) {
            if (groundState == 2 || dragon.isRunning()) {
                state.controller().setTransitionTicks(movingTransitionTicks);
                setAndContinue(state, runAnimation);
            } else if (groundState == 1 || dragon.isWalking() || (treatAnimationStateMovingAsWalk && state.isMoving())) {
                state.controller().setTransitionTicks(movingTransitionTicks);
                setAndContinue(state, walkAnimation);
            } else {
                state.controller().setTransitionTicks(idleTransitionTicks);
                setAndContinue(state, idleAnimation);
            }
            return PlayState.CONTINUE;
        }

        if (groundState == 2 || (state.isMoving() && dragon.shouldUseRunAnimation())) {
            state.controller().setTransitionTicks(movingTransitionTicks);
            setAndContinue(state, runAnimation);
        } else if (groundState == 1 || state.isMoving()) {
            state.controller().setTransitionTicks(movingTransitionTicks);
            setAndContinue(state, walkAnimation);
        } else {
            state.controller().setTransitionTicks(idleTransitionTicks);
            setAndContinue(state, idleAnimation);
        }
        return PlayState.CONTINUE;
    }


    public static RawAnimation loop(String dragonName, String animationName) {
        return RawAnimation.begin().thenLoop(path(dragonName, animationName));
    }

    private static String path(String dragonName, String animationName) {
        return "animation." + dragonName + "." + animationName;
    }

    public record Animations(
            RawAnimation idle,
            RawAnimation walk,
            RawAnimation run,
            RawAnimation sit,
            RawAnimation sitDown,
            RawAnimation sitUp,
            RawAnimation fallAsleep,
            RawAnimation sleep,
            RawAnimation wakeUp,
            RawAnimation swim,
            RawAnimation stunned,
            RawAnimation falling
    ) {
    }

    public record Transitions(
            int idle,
            int moving,
            int sit,
            int sleep,
            int bodyTransition,
            int water,
            int stunned,
            int falling
    ) {
    }

    public record FlightAnimations(
            RawAnimation takeoff,
            RawAnimation riderTakeoff,
            RawAnimation landing,
            RawAnimation flyGlide,
            RawAnimation glideDown,
            RawAnimation flyIdle,
            RawAnimation flap,
            RawAnimation sprintFlap
    ) {
    }

    public record FlightTransitions(
            int takeoff,
            int flyGlide,
            int glideDown,
            int landing,
            int flyIdle,
            int flap,
            int sprintFlap,
            int landed
    ) {
    }

    public interface SpecialStates<T extends RideableDragonBase> {
        default boolean riderControlsLocked(T dragon) {
            return dragon.areRiderControlsLocked();
        }

        default boolean tamingStunned(T dragon) {
            return false;
        }

        default boolean inWater(T dragon) {
            return dragon.isInWater() || dragon.isInWater();
        }

        default boolean falling(T dragon) {
            return false;
        }

        default PlayState handle(AnimationTest<T> state, T dragon, Animations animations, Transitions transitions) {
            return null;
        }
    }
}
