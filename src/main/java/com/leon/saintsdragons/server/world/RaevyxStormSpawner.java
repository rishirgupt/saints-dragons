package com.leon.saintsdragons.server.world;

import com.leon.saintsdragons.common.config.SaintsDragonsConfig;
import com.leon.saintsdragons.common.registry.ModEntities;
import com.leon.saintsdragons.common.registry.ModTags;
import com.leon.saintsdragons.common.world.DragonBiomeMatcher;
import com.leon.saintsdragons.server.entity.dragons.raevyx.Raevyx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class RaevyxStormSpawner {
    private static final int CHECK_INTERVAL = 120;
    private static final int HORIZONTAL_RADIUS = 96;
    private static final int SEARCH_ATTEMPTS = 24;
    private static final int CLUSTER_SIZE = 160;
    private static final int NEARBY_SEARCH_RADIUS = 128;

    private static final Map<Identifier, Integer> tickCounters = new HashMap<>();
    private static final Map<Identifier, Set<Long>> activeClusters = new HashMap<>();

    private RaevyxStormSpawner() {
    }

    public static void tick(ServerLevel level) {
        Identifier dimensionId = level.dimension().identifier();
        int counter = tickCounters.getOrDefault(dimensionId, 0) + 1;
        tickCounters.put(dimensionId, counter);
        if (counter < CHECK_INTERVAL) {
            return;
        }
        tickCounters.put(dimensionId, 0);

        int weight = SaintsDragonsConfig.RAEVYX_SPAWN_WEIGHT.get();
        if (weight <= 0 || level.players().isEmpty() || !DragonSpawnRules.hasWildRaevyxStorm(level)) {
            return;
        }

        // Stronger than vanilla creature spawning, but still conservative.
        int effectiveChance = Mth.clamp(weight * 2, 0, 100);
        if (level.getRandom().nextInt(100) >= effectiveChance) {
            return;
        }

        Player player = pickEligiblePlayer(level);
        if (player == null) {
            return;
        }

        BlockPos center = player.blockPosition();
        long clusterKey = getClusterKey(center);
        Set<Long> trackedClusters = activeClusters.computeIfAbsent(dimensionId, key -> new HashSet<>());
        if (trackedClusters.contains(clusterKey)) {
            if (hasRaevyxNearby(level, center)) {
                return;
            }
            trackedClusters.remove(clusterKey);
        }

        BlockPos spawnPos = findSpawnPos(level, center, level.getRandom());
        if (spawnPos == null) {
            return;
        }

        if (!DragonSpawnRules.passesNearbyDragonDensityCheck(level, EntitySpawnReason.NATURAL, spawnPos, Raevyx.class)) {
            return;
        }

        if (spawnPack(level, spawnPos, level.getRandom())) {
            trackedClusters.add(clusterKey);
        }
    }

    public static void clearTracking() {
        tickCounters.clear();
        activeClusters.clear();
    }

    private static Player pickEligiblePlayer(ServerLevel level) {
        var players = level.players().stream()
                .filter(player -> player.isAlive() && !player.isSpectator())
                .filter(player -> {
                    BlockPos pos = player.blockPosition();
                    return isRaevyxBiomeAllowed(level.getBiome(pos))
                            && level.canSeeSky(pos)
                            && !hasRaevyxNearby(level, pos);
                })
                .toList();
        if (players.isEmpty()) {
            return null;
        }
        return players.get(level.getRandom().nextInt(players.size()));
    }

    private static BlockPos findSpawnPos(ServerLevel level, BlockPos center, RandomSource random) {
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = center.getX() + random.nextInt(HORIZONTAL_RADIUS * 2 + 1) - HORIZONTAL_RADIUS;
            int z = center.getZ() + random.nextInt(HORIZONTAL_RADIUS * 2 + 1) - HORIZONTAL_RADIUS;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);

            if (!isRaevyxBiomeAllowed(level.getBiome(pos))) {
                continue;
            }
            if (!level.canSeeSky(pos)) {
                continue;
            }
            if (!Raevyx.canSpawnHere(ModEntities.RAEVYX.get(), level, EntitySpawnReason.NATURAL, pos, random)) {
                continue;
            }
            if (!canFitRaevyxAt(level, pos)) {
                continue;
            }
            return pos;
        }
        return null;
    }

    private static boolean spawnPack(ServerLevel level, BlockPos origin, RandomSource random) {
        int minGroupSize = SaintsDragonsConfig.RAEVYX_MIN_GROUP_SIZE.get();
        int maxGroupSize = SaintsDragonsConfig.RAEVYX_MAX_GROUP_SIZE.get();
        if (minGroupSize <= 0 || maxGroupSize <= 0) {
            return false;
        }
        if (minGroupSize > maxGroupSize) {
            minGroupSize = maxGroupSize;
        }

        int targetCount = Mth.nextInt(random, minGroupSize, maxGroupSize);
        int spawned = 0;

        if (spawnOne(level, origin, EntitySpawnReason.NATURAL)) {
            spawned++;
        } else {
            return false;
        }

        for (int i = 1; i < targetCount; i++) {
            BlockPos nearby = findNearbyPackPos(level, origin, random);
            if (nearby != null && spawnOne(level, nearby, EntitySpawnReason.EVENT)) {
                spawned++;
            }
        }

        return spawned > 0;
    }

    private static boolean spawnOne(ServerLevel level, BlockPos pos, EntitySpawnReason spawnType) {
        Raevyx raevyx = ModEntities.RAEVYX.get().create(level, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (raevyx == null) {
            return false;
        }

        raevyx.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
        if (!level.noCollision(raevyx, raevyx.getBoundingBox())) {
            return false;
        }
        raevyx.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), spawnType, null);
        if (!level.noCollision(raevyx, raevyx.getBoundingBox())) {
            return false;
        }
        return level.addFreshEntity(raevyx);
    }

    private static BlockPos findNearbyPackPos(ServerLevel level, BlockPos origin, RandomSource random) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = origin.getX() + random.nextInt(17) - 8;
            int z = origin.getZ() + random.nextInt(17) - 8;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.canSeeSky(pos)) {
                continue;
            }
            if (Raevyx.canSpawnHere(ModEntities.RAEVYX.get(), level, EntitySpawnReason.EVENT, pos, random)
                    && canFitRaevyxAt(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean canFitRaevyxAt(ServerLevel level, BlockPos pos) {
        Raevyx probe = ModEntities.RAEVYX.get().create(level, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (probe == null) {
            return false;
        }
        probe.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        AABB box = probe.getBoundingBox();
        return level.noCollision(probe, box);
    }

    private static boolean hasRaevyxNearby(ServerLevel level, BlockPos center) {
        return !level.getEntitiesOfClass(
                Raevyx.class,
                new AABB(center).inflate(NEARBY_SEARCH_RADIUS),
                dragon -> dragon.isAlive() && !dragon.isTame()
        ).isEmpty();
    }

    private static long getClusterKey(BlockPos pos) {
        int x = Math.floorDiv(pos.getX(), CLUSTER_SIZE);
        int z = Math.floorDiv(pos.getZ(), CLUSTER_SIZE);
        return (((long) x) << 32) ^ (z & 0xffffffffL);
    }

    private static boolean isRaevyxBiomeAllowed(Holder<Biome> biome) {
        return DragonBiomeMatcher.isAllowed(biome, ModTags.Biomes.HAS_RAEVYX);
    }
}
