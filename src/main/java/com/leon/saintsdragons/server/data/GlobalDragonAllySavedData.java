package com.leon.saintsdragons.server.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent per-player ally registry shared across all dragons.
 */
public class GlobalDragonAllySavedData extends SavedData {
    // 26.x: saved data is codec-based; the existing tag save/load code is reused through a compound codec.
    private static final com.mojang.serialization.Codec<GlobalDragonAllySavedData> CODEC = CompoundTag.CODEC.xmap(
            GlobalDragonAllySavedData::load, data -> data.save(new CompoundTag()));
    private static final net.minecraft.world.level.saveddata.SavedDataType<GlobalDragonAllySavedData> TYPE =
            new net.minecraft.world.level.saveddata.SavedDataType<>(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("saintsdragons", "global_allies"),
                    GlobalDragonAllySavedData::new, CODEC, net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private static final String DATA_NAME = "saintsdragons_global_allies";

    private final Map<UUID, Map<UUID, String>> alliesByOwner = new HashMap<>();

    public static GlobalDragonAllySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public Map<UUID, String> getAllies(UUID ownerId) {
        Map<UUID, String> allies = alliesByOwner.get(ownerId);
        if (allies == null) {
            return Map.of();
        }
        return new HashMap<>(allies);
    }

    public boolean isAlly(UUID ownerId, UUID allyId) {
        Map<UUID, String> allies = alliesByOwner.get(ownerId);
        if (allies == null) {
            return false;
        }
        return allies.containsKey(allyId);
    }

    public int getAllyCount(UUID ownerId) {
        Map<UUID, String> allies = alliesByOwner.get(ownerId);
        return allies == null ? 0 : allies.size();
    }

    public void addAlly(UUID ownerId, UUID allyId, String username) {
        alliesByOwner.computeIfAbsent(ownerId, id -> new HashMap<>()).put(allyId, username);
        setDirty();
    }

    public boolean removeAlly(UUID ownerId, UUID allyId) {
        Map<UUID, String> allies = alliesByOwner.get(ownerId);
        if (allies == null) {
            return false;
        }
        boolean removed = allies.remove(allyId) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public void clearAllies(UUID ownerId) {
        Map<UUID, String> allies = alliesByOwner.get(ownerId);
        if (allies != null && !allies.isEmpty()) {
            allies.clear();
            setDirty();
        }
    }

    
    public CompoundTag save(CompoundTag tag) {
        ListTag playerList = new ListTag();
        for (Map.Entry<UUID, Map<UUID, String>> entry : alliesByOwner.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.store("Owner", UUIDUtil.CODEC, entry.getKey());
            ListTag allyList = new ListTag();
            for (Map.Entry<UUID, String> allyEntry : entry.getValue().entrySet()) {
                CompoundTag allyTag = new CompoundTag();
                allyTag.store("UUID", UUIDUtil.CODEC, allyEntry.getKey());
                allyTag.putString("Username", allyEntry.getValue());
                allyList.add(allyTag);
            }
            playerTag.put("Allies", allyList);
            playerList.add(playerTag);
        }
        tag.put("Players", playerList);
        return tag;
    }

    public static GlobalDragonAllySavedData load(CompoundTag tag) {
        GlobalDragonAllySavedData data = new GlobalDragonAllySavedData();
        if (tag.contains("Players")) {
            ListTag playerList = tag.getListOrEmpty("Players");
            for (int i = 0; i < playerList.size(); i++) {
                CompoundTag playerTag = playerList.getCompoundOrEmpty(i);
                if (!playerTag.contains("Owner")) {
                    continue;
                }
                UUID ownerId = playerTag.read("Owner", UUIDUtil.CODEC).orElse(null);
                Map<UUID, String> allies = new HashMap<>();
                if (playerTag.contains("Allies")) {
                    ListTag allyList = playerTag.getListOrEmpty("Allies");
                    for (int j = 0; j < allyList.size(); j++) {
                        CompoundTag allyTag = allyList.getCompoundOrEmpty(j);
                        if (allyTag.contains("UUID") && allyTag.contains("Username")) {
                            allies.put(allyTag.read("UUID", UUIDUtil.CODEC).orElse(null), allyTag.getStringOr("Username", ""));
                        }
                    }
                }
                data.alliesByOwner.put(ownerId, allies);
            }
        }
        return data;
    }
}
