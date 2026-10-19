package com.leon.saintsdragons.server.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Ground paths that remember whether they stopped at the search boundary.
 * Path became final in 26.x, so the flag is tracked beside the path instead of in a subclass.
 */
public final class DragonGroundPath {
    private static final Map<Path, Boolean> FRONTIER_PATHS = Collections.synchronizedMap(new WeakHashMap<>());

    public static Path create(List<Node> nodes, BlockPos target, boolean reached, boolean frontier) {
        Path path = new Path(nodes, target, reached);
        if (frontier) {
            FRONTIER_PATHS.put(path, Boolean.TRUE);
        }
        return path;
    }

    public static boolean endsAtSearchBoundary(Path path) {
        return path != null && FRONTIER_PATHS.containsKey(path);
    }

    private DragonGroundPath() {
    }
}
