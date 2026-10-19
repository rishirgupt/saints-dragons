package com.leon.saintsdragons.server.entity.npc.dialogue;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DialogueRegistry {
    private static final Map<Identifier, DialogueDefinition> DEFINITIONS = new ConcurrentHashMap<>();

    private DialogueRegistry() {
    }

    public static void replaceDatapackDialogues(Map<Identifier, DialogueDefinition> dialogues) {
        DEFINITIONS.clear();
        DEFINITIONS.putAll(dialogues);
    }

    @Nullable
    public static DialogueDefinition get(Identifier id) {
        return DEFINITIONS.get(id);
    }
}
