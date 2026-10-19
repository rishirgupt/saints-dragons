package com.leon.saintsdragons.server.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DragonlordPlayerSavedData extends SavedData {
    // 26.x: saved data is codec-based; the existing tag save/load code is reused through a compound codec.
    private static final com.mojang.serialization.Codec<DragonlordPlayerSavedData> CODEC = CompoundTag.CODEC.xmap(
            DragonlordPlayerSavedData::load, data -> data.save(new CompoundTag()));
    private static final net.minecraft.world.level.saveddata.SavedDataType<DragonlordPlayerSavedData> TYPE =
            new net.minecraft.world.level.saveddata.SavedDataType<>(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("saintsdragons", "dragonlord_players"),
                    DragonlordPlayerSavedData::new, CODEC, net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private static final String DATA_NAME = "saintsdragons_dragonlord_players";
    private final Map<UUID, Float> savedHealth = new HashMap<>();

    public static DragonlordPlayerSavedData get(ServerLevel level) {
        ServerLevel storageLevel = level.getServer().overworld();
        return storageLevel.getDataStorage().computeIfAbsent(TYPE);
    }

    public void saveHealth(UUID playerId, float health) {
        if (playerId == null || health <= 0.0F) {
            return;
        }
        savedHealth.put(playerId, health);
        setDirty();
    }

    public Optional<Float> consumeHealth(UUID playerId) {
        if (playerId == null) {
            return Optional.empty();
        }
        Float health = savedHealth.remove(playerId);
        if (health != null) {
            setDirty();
        }
        return Optional.ofNullable(health);
    }

    public Optional<Float> getHealth(UUID playerId) {
        if (playerId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(savedHealth.get(playerId));
    }

    public void clearHealth(UUID playerId) {
        if (playerId != null && savedHealth.remove(playerId) != null) {
            setDirty();
        }
    }

    
    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();
        for (Map.Entry<UUID, Float> entry : savedHealth.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.store("UUID", UUIDUtil.CODEC, entry.getKey());
            playerTag.putFloat("Health", entry.getValue());
            players.add(playerTag);
        }
        tag.put("Players", players);
        return tag;
    }

    public static DragonlordPlayerSavedData load(CompoundTag tag) {
        DragonlordPlayerSavedData data = new DragonlordPlayerSavedData();
        if (tag.contains("Players")) {
            ListTag players = tag.getListOrEmpty("Players");
            for (int i = 0; i < players.size(); i++) {
                CompoundTag playerTag = players.getCompoundOrEmpty(i);
                if (playerTag.contains("UUID") && playerTag.contains("Health")) {
                    data.savedHealth.put(playerTag.read("UUID", UUIDUtil.CODEC).orElse(null), playerTag.getFloatOr("Health", 0.0F));
                }
            }
        }
        return data;
    }
}
