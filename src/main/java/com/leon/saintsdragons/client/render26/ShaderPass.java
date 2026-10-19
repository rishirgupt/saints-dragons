package com.leon.saintsdragons.client.render26;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Tells whether a shader mod (Iris) is currently drawing its shadow pass.
 *
 * In that pass entities are drawn from the sun's point of view, so matrices on the pose stack are not
 * relative to the player's camera. Anything that turns a pose into a world position (rider seats, beam and
 * breath origins) must skip it, or riders and effects end up far away from the dragon.
 */
public final class ShaderPass {
    private static final MethodHandle IRIS_INSTANCE;
    private static final MethodHandle IRIS_IS_SHADOW_PASS;

    static {
        MethodHandle instance = null;
        MethodHandle shadowPass = null;
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                instance = lookup.findStatic(api, "getInstance", MethodType.methodType(api));
                shadowPass = lookup.findVirtual(api, "isRenderingShadowPass", MethodType.methodType(boolean.class));
            } catch (ReflectiveOperationException | LinkageError ignored) {
                instance = null;
                shadowPass = null;
            }
        }
        IRIS_INSTANCE = instance;
        IRIS_IS_SHADOW_PASS = shadowPass;
    }

    private ShaderPass() {
    }

    public static boolean isShadowPass() {
        if (IRIS_INSTANCE == null || IRIS_IS_SHADOW_PASS == null) {
            return false;
        }
        try {
            return (boolean) IRIS_IS_SHADOW_PASS.invoke(IRIS_INSTANCE.invoke());
        } catch (Throwable ignored) {
            return false;
        }
    }
}
