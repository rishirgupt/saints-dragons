package com.leon.saintsdragons.client.render26;

import org.joml.Matrix3f;
import org.joml.Vector3f;

/** Normal transformation by a 3x3 normal matrix, for vertex code written against the 1.20.1 consumer. */
public final class Normals {
    private Normals() {
    }

    public static float x(Matrix3f matrix, float x, float y, float z) {
        return matrix.transform(new Vector3f(x, y, z)).x;
    }

    public static float y(Matrix3f matrix, float x, float y, float z) {
        return matrix.transform(new Vector3f(x, y, z)).y;
    }

    public static float z(Matrix3f matrix, float x, float y, float z) {
        return matrix.transform(new Vector3f(x, y, z)).z;
    }
}
