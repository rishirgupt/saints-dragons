package com.leon.saintsdragons.server.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.ServerLevelData;

public final class RaevyxStormSavedData extends SavedData {
    // 26.x: saved data is codec-based; the existing tag save/load code is reused through a compound codec.
    private static final com.mojang.serialization.Codec<RaevyxStormSavedData> CODEC = CompoundTag.CODEC.xmap(
            RaevyxStormSavedData::load, data -> data.save(new CompoundTag()));
    private static final net.minecraft.world.level.saveddata.SavedDataType<RaevyxStormSavedData> TYPE =
            new net.minecraft.world.level.saveddata.SavedDataType<>(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("saintsdragons", "raevyx_storm"),
                    RaevyxStormSavedData::new, CODEC, net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private boolean summonedStorm;
    private int naturalThunderTicks;

    private static RaevyxStormSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static void beforeSummoning(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        RaevyxStormSavedData state = get(overworld);
        if (!state.summonedStorm) {
            var weather = overworld.getWeatherData();
            state.naturalThunderTicks = weather.isThundering() ? Math.max(0, weather.getThunderTime()) : 0;
        }
        state.summonedStorm = true;
        state.setDirty();
    }

    public static void tick(ServerLevel level) {
        if (level != level.getServer().overworld()) return;
        RaevyxStormSavedData state = get(level);
        if (!state.summonedStorm) return;
        if (!level.getWeatherData().isThundering()) {
            state.naturalThunderTicks = 0;
            if (!level.isThundering()) state.summonedStorm = false;
            state.setDirty();
        } else if (state.naturalThunderTicks > 0
                && level.getGameRules().get(GameRules.ADVANCE_WEATHER)) {
            state.naturalThunderTicks--;
            state.setDirty();
        }
    }

    public static boolean allowsWildSpawns(ServerLevel level) {
        if (!level.isThundering()) return false;
        if (!level.getServer().isSameThread()) return false;
        RaevyxStormSavedData state = get(level);
        return !state.summonedStorm || state.naturalThunderTicks > 0;
    }

    private static RaevyxStormSavedData load(CompoundTag tag) {
        RaevyxStormSavedData state = new RaevyxStormSavedData();
        state.summonedStorm = tag.getBooleanOr("SummonedStorm", false);
        state.naturalThunderTicks = Math.max(0, tag.getIntOr("NaturalThunderTicks", 0));
        return state;
    }

    
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("SummonedStorm", summonedStorm);
        tag.putInt("NaturalThunderTicks", naturalThunderTicks);
        return tag;
    }
}
