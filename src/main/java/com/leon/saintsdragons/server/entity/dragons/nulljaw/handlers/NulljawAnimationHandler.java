package com.leon.saintsdragons.server.entity.dragons.nulljaw.handlers;

import com.leon.saintsdragons.util.animation.AnimationHelper;

import com.leon.saintsdragons.server.entity.dragons.nulljaw.Nulljaw;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;

public final class NulljawAnimationHandler {
    public static final String ACTION_CONTROLLER = "actions";
    public static final String BITE_TRIGGER = "bite";

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.nulljaw.idle");
    private static final RawAnimation HOVER = RawAnimation.begin().thenLoop("animation.nulljaw.hover");
    private static final RawAnimation HAND_HOLDING = RawAnimation.begin().thenLoop("animation.nulljaw.hand_holding");
    private static final RawAnimation EAT = RawAnimation.begin().thenPlay("animation.nulljaw.eat");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.nulljaw.hurt");
    private static final RawAnimation DIE = RawAnimation.begin().thenPlay("animation.nulljaw.die");
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("animation.nulljaw.bite");
    private static final int MOVEMENT_TRANSITION_TICKS = 2;
    private static final int ACTION_TRANSITION_TICKS = 2;
    private static final int INSTANT_TRANSITION_TICKS = 1;
    private static final int MOUNTED_TRANSITION_TICKS = 2;

    private final Nulljaw dragon;

    public NulljawAnimationHandler(Nulljaw dragon) {
        this.dragon = dragon;
    }

    public PlayState movementPredicate(AnimationTest<Nulljaw> state) {
        if (dragon.isDeadOrDying()) {
            return PlayState.STOP;
        }

        PlayState dance = AnimationHelper.tryHandleDance(state, dragon, MOVEMENT_TRANSITION_TICKS);
        if (dance != null) {
            return dance;
        }

        state.controller().setTransitionTicks(MOVEMENT_TRANSITION_TICKS);
        if (dragon.isMovingForAnimation()) {
            AnimationHelper.setAndContinue(state, HOVER);
        } else {
            AnimationHelper.setAndContinue(state, IDLE);
        }
        return PlayState.CONTINUE;
    }

    public PlayState actionPredicate(AnimationTest<Nulljaw> state) {
        state.controller().setTransitionTicks(ACTION_TRANSITION_TICKS);
        return PlayState.STOP;
    }

    public PlayState instantPredicate(AnimationTest<Nulljaw> state) {
        state.controller().setTransitionTicks(INSTANT_TRANSITION_TICKS);
        return PlayState.STOP;
    }

    public PlayState mountedPredicate(AnimationTest<Nulljaw> state) {
        state.controller().setTransitionTicks(MOUNTED_TRANSITION_TICKS);
        if (!dragon.isVehicle()) {
            return PlayState.STOP;
        }

        AnimationHelper.setAndContinue(state, HAND_HOLDING);
        return PlayState.CONTINUE;
    }

    public void setupActionController(AnimationController<Nulljaw> controller) {
        controller.triggerableAnim(BITE_TRIGGER, BITE);
    }

    public void setupInstantController(AnimationController<Nulljaw> controller) {
    }

    public void setupInteractionController(AnimationController<Nulljaw> controller) {
        controller.triggerableAnim(AnimationHelper.EAT, EAT);
        controller.triggerableAnim("nulljaw_hurt", HURT);
        controller.triggerableAnim("nulljaw_die", DIE);
    }
}
