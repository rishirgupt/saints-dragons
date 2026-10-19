package com.leon.saintsdragons.server.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WikiReminderSavedData extends SavedData {
    // 26.x: saved data is codec-based; the existing tag save/load code is reused through a compound codec.
    private static final com.mojang.serialization.Codec<WikiReminderSavedData> CODEC = CompoundTag.CODEC.xmap(
            WikiReminderSavedData::load, data -> data.save(new CompoundTag()));
    private static final net.minecraft.world.level.saveddata.SavedDataType<WikiReminderSavedData> TYPE =
            new net.minecraft.world.level.saveddata.SavedDataType<>(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("saintsdragons", "wiki_reminders"),
                    WikiReminderSavedData::new, CODEC, net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private static final String DATA_NAME = "saintsdragons_wiki_reminders";
    private final Set<UUID> shownPlayers = new HashSet<>();

    public static WikiReminderSavedData get(ServerLevel level) {
        ServerLevel storageLevel = level.getServer().overworld();
        return storageLevel.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean markShownIfFirst(UUID playerId) {
        boolean added = shownPlayers.add(playerId);
        if (added) {
            setDirty();
        }
        return added;
    }

    
    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();
        for (UUID playerId : shownPlayers) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.store("UUID", UUIDUtil.CODEC, playerId);
            players.add(playerTag);
        }
        tag.put("Players", players);
        return tag;
    }

    public static WikiReminderSavedData load(CompoundTag tag) {
        WikiReminderSavedData data = new WikiReminderSavedData();
        if (tag.contains("Players")) {
            ListTag players = tag.getListOrEmpty("Players");
            for (int i = 0; i < players.size(); i++) {
                CompoundTag playerTag = players.getCompoundOrEmpty(i);
                if (playerTag.contains("UUID")) {
                    data.shownPlayers.add(playerTag.read("UUID", UUIDUtil.CODEC).orElse(null));
                }
            }
        }
        return data;
    }
}