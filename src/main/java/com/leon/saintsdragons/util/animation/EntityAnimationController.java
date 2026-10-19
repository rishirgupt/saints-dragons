package com.leon.saintsdragons.util.animation;

import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.leon.saintsdragons.server.entity.base.RideableDragonBase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mod-side controller base. GeckoLib 5 controllers no longer hold their animatable, so it is kept here
 * for subclasses that need entity state.
 *
 * A dragon's movement controller also gets a {@link BodyGuard}: GeckoLib 5 poses a bone only while some
 * controller animates it, and drops it to the bind pose (the T-pose) the moment none does.
 */
public class EntityAnimationController<T extends LivingEntity & TickingGeoEntity> extends ReleasingAnimationController<T> {
    protected final T animatable;

    public EntityAnimationController(T entity, String name, int transitionTicks,
                                     AnimationStateHandler<T> predicate) {
        this(entity, name, transitionTicks, predicate,
                AnimationHelper.MOVEMENT_CONTROLLER.equals(name) && entity instanceof DragonEntity);
    }

    private EntityAnimationController(T entity, String name, int transitionTicks,
                                      AnimationStateHandler<T> predicate, boolean guardBody) {
        super(name, transitionTicks, guardBody ? new BodyGuard<T>(entity, predicate) : predicate);
        this.animatable = entity;
    }

    /**
     * Keeps a dragon's body posed while its state handler has nothing to play.
     *
     * The ground handlers answer STOP in transitional states (sitting down, falling asleep, a dodge) and rely on a
     * triggered clip, or on the clip that was playing before, to carry the pose. When that clip is missing
     * (the trigger arrived late, the dragon was hidden or culled while it fired, the clip ended before the state
     * did) the whole model snaps to the T-pose until the state resolves. The guard fills such gaps with a resting
     * pose, and holds one-shot clips on their last frame instead of letting them fall back to the bind pose.
     */
    static final class BodyGuard<T extends LivingEntity & TickingGeoEntity> implements AnimationStateHandler<T> {
        private final T dragon;
        private final AnimationStateHandler<T> inner;
        private final Map<RawAnimation, RawAnimation> held = new IdentityHashMap<>();
        private RawAnimation idle;
        private RawAnimation sit;
        private RawAnimation sleep;

        BodyGuard(T dragon, AnimationStateHandler<T> inner) {
            this.dragon = dragon;
            this.inner = inner;
        }

        @Override
        public PlayState handle(AnimationTest<T> state) {
            PlayState result = inner.handle(state);
            var controller = state.controller();
            if (controller.isPlayingTriggeredAnimation()) {
                return result;
            }
            if (result != PlayState.STOP) {
                RawAnimation current = controller.getCurrentRawAnimation();
                if (current != null) {
                    RawAnimation hold = holdLastFrame(current);
                    if (hold != current) {
                        state.setAnimation(hold);
                    }
                }
                return result;
            }
            if (dragon.isDeadOrDying() || drivenElsewhere(state)) {
                return result;
            }
            RawAnimation rest = restPose();
            if (rest == null) {
                return result;
            }
            state.setAnimation(rest);
            return PlayState.CONTINUE;
        }

        /** True while another full-body controller (flight, death) is posing the dragon. */
        private boolean drivenElsewhere(AnimationTest<T> state) {
            if (dragon instanceof RideableDragonBase rideable
                    && (rideable.isFlying() || rideable.isTakeoff() || rideable.isLanding() || rideable.isHovering())) {
                return true;
            }
            var controllers = state.manager().getAnimationControllers();
            var flight = controllers.get(AnimationHelper.FLIGHT_CONTROLLER);
            if (flight != null && flight.isAnimatingBones()) {
                return true;
            }
            var interaction = controllers.get(AnimationHelper.INTERACTION_CONTROLLER);
            return interaction != null && interaction.isAnimatingBones();
        }

        private RawAnimation restPose() {
            String species = BuiltInRegistries.ENTITY_TYPE.getKey(dragon.getType()).getPath();
            if (idle == null) {
                idle = RawAnimation.begin().thenLoop("animation." + species + ".idle");
                // Species without sit and sleep clips (the nulljaw) just stand.
                boolean rests = !"nulljaw".equals(species);
                sit = rests ? RawAnimation.begin().thenLoop("animation." + species + ".sit") : idle;
                sleep = rests ? RawAnimation.begin().thenLoop("animation." + species + ".sleep") : idle;
            }
            if (dragon instanceof DragonEntity dragonEntity) {
                if (dragonEntity.isSleeping() || dragonEntity.isSleepingEntering()) {
                    return sleep;
                }
                if (dragonEntity.getSitProgress() > 0.0F && dragonEntity.isOrderedToSit()) {
                    return sit;
                }
            }
            return idle;
        }

        /** The same clips with a final one-shot stage held on its last frame. */
        private RawAnimation holdLastFrame(RawAnimation animation) {
            List<RawAnimation.Stage> stages = animation.getAnimationStages();
            if (stages.isEmpty()) {
                return animation;
            }
            RawAnimation.Stage last = stages.get(stages.size() - 1);
            if (last.loopType() != LoopType.PLAY_ONCE || RawAnimation.Stage.WAIT.equals(last.animationName())) {
                return animation;
            }
            RawAnimation cached = held.get(animation);
            if (cached != null) {
                return cached;
            }
            RawAnimation copy = RawAnimation.begin();
            for (int i = 0; i < stages.size(); i++) {
                RawAnimation.Stage stage = stages.get(i);
                if (RawAnimation.Stage.WAIT.equals(stage.animationName())) {
                    copy.thenWait(stage.waitTicks());
                } else {
                    copy.then(stage.animationName(), i == stages.size() - 1 ? LoopType.HOLD_ON_LAST_FRAME : stage.loopType());
                }
            }
            held.put(animation, copy);
            held.put(copy, copy);
            return copy;
        }
    }
}
