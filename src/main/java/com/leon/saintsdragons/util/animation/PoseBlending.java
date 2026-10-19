package com.leon.saintsdragons.util.animation;

import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.animation.state.ControllerState;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.loading.math.MathParser;
import com.geckolib.loading.math.MolangQueries;
import com.geckolib.loading.math.value.Variable;
import com.geckolib.renderer.base.GeoRenderState;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Shared pieces for controllers that blend several clips per bone on GeckoLib 5.
 * A pose is nine floats: rotation offsets from the rest pose (radians), translation, scale.
 */
final class PoseBlending {
    static final int ROTATION = 1;
    static final int POSITION = 2;
    static final int SCALE = 4;

    private final Quaternionf rotation = new Quaternionf();
    private final Quaternionf otherRotation = new Quaternionf();
    private final Vector3f angles = new Vector3f();

    static int channels(BoneAnimation track) {
        return (track.rotationKeyFrames().xKeyframes().length == 0 ? 0 : ROTATION)
                | (track.positionKeyFrames().xKeyframes().length == 0 ? 0 : POSITION)
                | (track.scaleKeyFrames().xKeyframes().length == 0 ? 0 : SCALE);
    }

    static float[] capture(BoneSnapshot snapshot) {
        return new float[] {
                snapshot.getRotX(), snapshot.getRotY(), snapshot.getRotZ(),
                snapshot.getTranslateX(), snapshot.getTranslateY(), snapshot.getTranslateZ(),
                snapshot.getScaleX(), snapshot.getScaleY(), snapshot.getScaleZ()};
    }

    void mix(float[] from, float[] to, float weight, float[] result, GeoBone bone) {
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

    static void writePose(BoneSnapshot snapshot, float[] pose, int channels) {
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

    /** Evaluates one clip at a fixed time (seconds) through GeckoLib's own keyframe evaluation. */
    static final class Sampler {
        private final @Nullable AnimationPoint point;
        private final @Nullable ControllerState sampleState;

        Sampler(@Nullable Animation animation, double time, GeoRenderState renderState,
                Reference2DoubleMap<Variable> queryValues) {
            if (animation == null) {
                this.point = null;
                this.sampleState = null;
                return;
            }
            this.point = AnimationPoint.createFor(animation, null, animation.loopType(), time);
            Reference2DoubleMap<Variable> values = new Reference2DoubleOpenHashMap<>(queryValues);
            values.put(MathParser.getVariableFor(MolangQueries.ANIM_TIME), time);
            this.sampleState = new ControllerState(point, null, -1, 0, false, null, renderState, values);
        }

        void sample(BoneSnapshot snapshot, @Nullable Integer track, float[] pose) {
            pose[0] = pose[1] = pose[2] = pose[3] = pose[4] = pose[5] = 0.0F;
            pose[6] = pose[7] = pose[8] = 1.0F;
            if (track == null || point == null || sampleState == null) return;
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
