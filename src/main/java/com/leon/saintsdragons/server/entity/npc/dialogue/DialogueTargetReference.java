package com.leon.saintsdragons.server.entity.npc.dialogue;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

record DialogueTargetReference(Identifier dialogueId, String nodeId, boolean external) {
    @Nullable
    static DialogueTargetReference parse(Identifier currentDialogueId, String value) {
        int split = value.indexOf('#');
        if (split < 0) {
            return new DialogueTargetReference(currentDialogueId, value, false);
        }
        if (split == 0 || split == value.length() - 1) {
            return null;
        }
        Identifier dialogueId = Identifier.tryParse(value.substring(0, split));
        if (dialogueId == null) {
            return null;
        }
        return new DialogueTargetReference(dialogueId, value.substring(split + 1), true);
    }
}
