package com.leon.saintsdragons.client.render26;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.block.state.BlockState;

/** Draws a real block model in place of each cube of a GeckoLib bone (used by "chunk of ground" models). */
public final class BlockCubes {
    private BlockCubes() {
    }

    /**
     * @param scale 1 for a full block; smaller values shrink each block around its centre
     */
    public static void render(GeoBone bone, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource,
                              int packedLight, int packedOverlay, float scale) {
        if (state.isAir() || !(bone instanceof CuboidGeoBone cuboid)) {
            return;
        }
        BlockRenderDispatcher blockRenderer = BlockRenderDispatcher.get();
        for (GeoCube cube : cuboid.cubes) {
            double[] center = center(cube);
            poseStack.pushPose();
            poseStack.translate(center[0] - 0.5D, center[1] - 0.5D, center[2] - 0.5D);
            if (scale < 0.999F) {
                float clamped = Math.max(scale, 0.001F);
                poseStack.translate(0.5D, 0.5D, 0.5D);
                poseStack.scale(clamped, clamped, clamped);
                poseStack.translate(-0.5D, -0.5D, -0.5D);
            }
            blockRenderer.renderSingleBlock(state, poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static double[] center(GeoCube cube) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        if (cube.quads() != null) {
            for (GeoQuad quad : cube.quads()) {
                if (quad == null) {
                    continue;
                }
                for (GeoVertex vertex : quad.vertices()) {
                    if (vertex == null) {
                        continue;
                    }
                    minX = Math.min(minX, vertex.posX());
                    minY = Math.min(minY, vertex.posY());
                    minZ = Math.min(minZ, vertex.posZ());
                    maxX = Math.max(maxX, vertex.posX());
                    maxY = Math.max(maxY, vertex.posY());
                    maxZ = Math.max(maxZ, vertex.posZ());
                }
            }
        }
        if (!Double.isFinite(minX)) {
            return new double[] {0.0D, 0.0D, 0.0D};
        }
        return new double[] {(minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D};
    }
}
