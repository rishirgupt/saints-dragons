package com.leon.saintsdragons.client.renderer.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.util.RenderUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.leon.saintsdragons.util.WeakIdentityCache;

public final class DragonBoneSurfaceSampler {
    private static final WeakIdentityCache<GeoBone, List<Face>> SURFACES = new WeakIdentityCache<>();
    private static final double SURFACE_OFFSET = 0.06;
    record Face(Vec3 origin, Vec3 edgeU, Vec3 edgeV, Vec3 normal) {}
    record WeightedFace(Face face, double cumulativeArea) {}

    public static AABB bounds(BakedGeoModel model, Map<String, Matrix4f> transforms, String[] names) {
        AABB result = null;
        for (String name : names) {
            GeoBone bone = model.getBone(name).orElse(null);
            Matrix4f transform = transforms.get(name);
            if (bone == null || transform == null || !isVisible(bone)) continue;
            for (Face face : SURFACES.computeIfAbsent(bone, DragonBoneSurfaceSampler::cacheSurfaces)) {
                for (int corner = 0; corner < 4; corner++) {
                    Vec3 local = face.origin().add(face.edgeU().scale(corner & 1)).add(face.edgeV().scale((corner >> 1) & 1));
                    Vec3 point = transformPosition(transform, local);
                    var box = new AABB(point, point);
                    result = result == null ? box : result.minmax(box);
                }
            }
        }
        return result == null ? null : result.inflate(0.12D);
    }
    static List<WeightedFace> animatedSurfaces(BakedGeoModel model, Map<String, Matrix4f> transforms, String[][] anchors) {
        List<WeightedFace> result = new ArrayList<>();
        double totalArea = 0.0;
        for (String[] names : anchors) {
            GeoBone bone = null;
            for (String name : names) {
                bone = model.getBone(name).orElse(null);
                if (bone != null) break;
            }
            if (bone == null || !isVisible(bone)) continue;
            Matrix4f transform = transforms.get(bone.name());
            if (transform == null) continue;
            for (Face local : SURFACES.computeIfAbsent(bone, DragonBoneSurfaceSampler::cacheSurfaces)) {
                Vec3 origin = transformPosition(transform, local.origin());
                Vec3 edgeU = transformDirection(transform, local.edgeU());
                Vec3 edgeV = transformDirection(transform, local.edgeV());
                Vec3 cross = edgeU.cross(edgeV);
                double area = cross.length();
                if (!Double.isFinite(area) || area < 1.0E-8) continue;
                Vec3 normal = cross.scale(1.0 / area);
                if (normal.dot(transformDirection(transform, local.normal())) < 0) {
                    normal = normal.scale(-1);
                }
                totalArea += area;
                result.add(new WeightedFace(new Face(origin, edgeU, edgeV, normal), totalArea));
            }
        }
        return result;
    }

    private static boolean isVisible(GeoBone bone) {
        // Hidden bones are left out of the transform map when it is captured, so a present transform means visible.
        return true;
    }

    private static List<Face> cacheSurfaces(GeoBone bone) {
        List<Face> faces = new ArrayList<>();
        for (GeoCube cube : bone instanceof com.geckolib.cache.model.cuboid.CuboidGeoBone cuboid ? cuboid.cubes : new GeoCube[0]) {
            Vec3 size = cube.size();
            if (size.x <= 1.0E-6 || size.y <= 1.0E-6 || size.z <= 1.0E-6 || cube.quads() == null) continue;
            PoseStack poses = new PoseStack();
            cube.translateToPivotPoint(poses);
            cube.rotate(poses);
            cube.translateAwayFromPivotPoint(poses);
            Matrix4f transform = poses.last().pose();
            for (GeoQuad quad : cube.quads()) {
                if (quad == null || quad.vertices().length != 4) continue;
                Vec3 origin = transformPosition(transform, vector(position(quad.vertices()[0])));
                Vec3 edgeU = transformPosition(transform, vector(position(quad.vertices()[1]))).subtract(origin);
                Vec3 edgeV = transformPosition(transform, vector(position(quad.vertices()[3]))).subtract(origin);
                if (edgeU.cross(edgeV).lengthSqr() < 1.0E-12) continue;
                Vec3 normal = transformDirection(transform, vector(quad.normalVec())).normalize();
                faces.add(new Face(origin, edgeU, edgeV, normal));
            }
        }
        return List.copyOf(faces);
    }

    static Vec3 sample(List<WeightedFace> faces, double area, RandomSource random) {
        int low = 0, high = faces.size() - 1;
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (area < faces.get(middle).cumulativeArea()) high = middle;
            else low = middle + 1;
        }
        Face face = faces.get(low).face();
        return face.origin().add(face.edgeU().scale(random.nextDouble()))
                .add(face.edgeV().scale(random.nextDouble())).add(face.normal().scale(SURFACE_OFFSET));
    }

    private static Vec3 transformPosition(Matrix4fc matrix, Vec3 position) {
        return vector(matrix.transformPosition(position.toVector3f()));
    }

    private static Vec3 transformDirection(Matrix4fc matrix, Vec3 direction) {
        return vector(matrix.transformDirection(direction.toVector3f()));
    }

    private static Vector3f position(com.geckolib.cache.model.GeoVertex vertex) {
        return new Vector3f(vertex.posX(), vertex.posY(), vertex.posZ());
    }

    private static Vec3 vector(Vector3f vector) {
        return new Vec3(vector.x(), vector.y(), vector.z());
    }
}
