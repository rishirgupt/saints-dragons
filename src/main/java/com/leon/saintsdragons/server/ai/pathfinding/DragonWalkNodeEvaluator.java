package com.leon.saintsdragons.server.ai.pathfinding;

import com.leon.saintsdragons.server.entity.dragons.util.DragonDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class DragonWalkNodeEvaluator extends WalkNodeEvaluator implements DragonPathSearchDebuggable {
    private final BooleanSupplier canPassThroughTrees;
    private final boolean avoidWater;
    private @Nullable DragonPathSearchDebug.NodeCollector pathSearchDebugCollector;

    public DragonWalkNodeEvaluator() {
        this(false, false);
    }

    public DragonWalkNodeEvaluator(boolean canPassThroughTrees) {
        this(canPassThroughTrees, false);
    }

    public DragonWalkNodeEvaluator(boolean canPassThroughTrees, boolean avoidWater) {
        this(() -> canPassThroughTrees, avoidWater);
    }

    public DragonWalkNodeEvaluator(BooleanSupplier canPassThroughTrees) {
        this(canPassThroughTrees, false);
    }

    public DragonWalkNodeEvaluator(BooleanSupplier canPassThroughTrees, boolean avoidWater) {
        this.canPassThroughTrees = canPassThroughTrees;
        this.avoidWater = avoidWater;
    }

    @Override
    public void setPathSearchDebugCollector(@Nullable DragonPathSearchDebug.NodeCollector collector) {
        this.pathSearchDebugCollector = collector;
    }

    @Override
    public int getNeighbors(Node[] neighbors, Node current) {
        int count = super.getNeighbors(neighbors, current);
        if (this.pathSearchDebugCollector != null) {
            this.pathSearchDebugCollector.recordExpansion(current, neighbors, count);
        }
        return count;
    }

    @Override
    public @NotNull Node getStart() {
        Node vanillaStart = super.getStart();
        int footprintOffset = getFootprintOffset();
        if (footprintOffset == 0) {
            return vanillaStart;
        }

        return getStartNode(new BlockPos(
                this.mob.getBlockX() - footprintOffset,
                vanillaStart.y,
                this.mob.getBlockZ() - footprintOffset
        ));
    }

    @Override
    public @NotNull Target getTarget(double x, double y, double z) {
        int footprintOffset = getFootprintOffset();
        return new Target(getNode(
                Mth.floor(x) - footprintOffset,
                Mth.floor(y),
                Mth.floor(z) - footprintOffset
        ));
    }

    private int getFootprintOffset() {
        return Math.max(0, this.entityWidth / 2);
    }

    @Override
    public @NotNull PathType getPathType(net.minecraft.world.level.pathfinder.PathfindingContext context, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = context.getBlockState(pos);
        if (state.is(Blocks.LADDER)) {
            return PathType.WALKABLE;
        }
        if (this.canPassThroughTrees.getAsBoolean()
                && DragonDestructionManager.isPassivelyBreakableTreeBlock(state)) {
            return PathType.OPEN;
        }
        PathType pathType = super.getPathType(context, x, y, z);
        if (avoidWater && (pathType == PathType.WATER || pathType == PathType.WATER_BORDER)) {
            return PathType.BLOCKED;
        }
        return pathType;
    }

    /**
     * Vanilla static path typing, evaluated against a plain block getter (the async search works on an
     * immutable snapshot and has no PathfindingContext). Mirrors WalkNodeEvaluator#getPathTypeStatic.
     */
    public static PathType snapshotPathType(BlockGetter level, BlockPos.MutableBlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        PathType blockPathType = getPathTypeFromState(level, pos);
        if (blockPathType != PathType.OPEN || y < level.getMinY() + 1) {
            return blockPathType;
        }
        PathType below = getPathTypeFromState(level, pos.set(x, y - 1, z));
        pos.set(x, y, z);
        switch (below) {
            case OPEN, WATER, LAVA, WALKABLE: return PathType.OPEN;
            case FIRE: return PathType.FIRE;
            case DAMAGING: return PathType.DAMAGING;
            case STICKY_HONEY: return PathType.STICKY_HONEY;
            case POWDER_SNOW: return PathType.ON_TOP_OF_POWDER_SNOW;
            case DAMAGE_CAUTIOUS: return PathType.DAMAGE_CAUTIOUS;
            case TRAPDOOR: return PathType.ON_TOP_OF_TRAPDOOR;
            default: break;
        }
        PathType result = PathType.WALKABLE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    PathType neighbour = getPathTypeFromState(level, cursor.set(x + dx, y + dy, z + dz));
                    if (neighbour == PathType.DAMAGING) return PathType.DAMAGING_IN_NEIGHBOR;
                    if (neighbour == PathType.FIRE || neighbour == PathType.LAVA) return PathType.FIRE_IN_NEIGHBOR;
                    if (neighbour == PathType.WATER) return PathType.WATER_BORDER;
                    if (neighbour == PathType.DAMAGE_CAUTIOUS) return PathType.DAMAGE_CAUTIOUS;
                }
            }
        }
        return result;
    }
}
