package com.leon.saintsdragons.util.animation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

/**
 * Animation controller that hands control back to its state handler once a triggered animation has played
 * out, as GeckoLib 4 did.
 *
 * GeckoLib 5 keeps a controller marked as "triggered" after the triggered animation finishes. A controller
 * that does not opt into handling triggers itself then never consults its handler again, so after one
 * triggered clip (sitting down, taking off, an attack) it stops animating and the model falls back to its
 * rest pose.
 */
public class ReleasingAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
    public ReleasingAnimationController(String name, int transitionTicks, AnimationStateHandler<T> handler) {
        super(name, transitionTicks, keepTriggeredClip(handler));
    }

    public ReleasingAnimationController(String name, AnimationStateHandler<T> handler) {
        super(name, keepTriggeredClip(handler));
    }

    /**
     * A handler that opted into seeing triggered animations may answer STOP while one is playing, meaning
     * "nothing of mine to play". GeckoLib 4 let the triggered clip finish in that case; GeckoLib 5 would cut
     * it short, so STOP is turned into CONTINUE for as long as the triggered clip is still running.
     */
    private static <A extends GeoAnimatable> AnimationStateHandler<A> keepTriggeredClip(AnimationStateHandler<A> handler) {
        return state -> {
            com.geckolib.animation.object.PlayState result = handler.handle(state);
            if (result == com.geckolib.animation.object.PlayState.STOP
                    && state.controller() instanceof ReleasingAnimationController<?> controller
                    && controller.isPlayingTriggeredAnimation()) {
                return com.geckolib.animation.object.PlayState.CONTINUE;
            }
            return result;
        };
    }

    /**
     * True from the moment a triggered clip is requested until it has played out. GeckoLib only reports it
     * once the clip has been set up on the next frame, which lets a handler replace the clip in between.
     */
    @Override
    public boolean isPlayingTriggeredAnimation() {
        return this.triggeredAnimTime >= 0 && !hasAnimationFinished();
    }

    @Override
    protected boolean checkControllerState(T animatable, GeoRenderState renderState, AnimatableManager<T> manager,
                                           GeoModel<T> geoModel) {
        if (this.triggeredAnimTime >= 0 && hasAnimationFinished()) {
            // The triggered clip is over: let the handler pick what plays next (it blends from the last pose).
            this.triggeredAnimTime = NOT_TRIGGERED;
        }
        boolean animating = super.checkControllerState(animatable, renderState, manager, geoModel);
        return releaseWhenStopped(renderState.getAnimatableAge()) && animating;
    }

    /** Age of the animatable when the handler last wanted something played; NaN while it does. */
    private double stoppedSince = Double.NaN;

    /**
     * GeckoLib 4 stopped animating a controller once its handler answered STOP. GeckoLib 5 lets a looping clip
     * (a landing or hover loop, say) keep running after STOP, so that controller goes on overriding every
     * other controller's pose for good. Once a stopped controller has had time to blend out, end it.
     *
     * @return false when the controller was ended on this frame
     */
    private boolean releaseWhenStopped(double age) {
        boolean stopped = this.playState == com.geckolib.animation.object.PlayState.STOP
                && this.triggeredAnimTime == NOT_TRIGGERED;
        if (!stopped || !isAnimatingBones()) {
            this.stoppedSince = Double.NaN;
            return true;
        }
        if (Double.isNaN(this.stoppedSince) || age < this.stoppedSince) {
            this.stoppedSince = age;
            return true;
        }
        if (age - this.stoppedSince > this.transitionTicks + 4) {
            this.timelineTime = FINISHED_ANIMATING;
            this.stoppedSince = Double.NaN;
            return false;
        }
        return true;
    }
}
