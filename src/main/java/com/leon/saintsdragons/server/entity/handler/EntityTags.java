package com.leon.saintsdragons.server.entity.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Whole-entity NBT snapshots through the ValueOutput/ValueInput storage API (1.21.6+). */
public final class EntityTags {
    public static CompoundTag save(Entity entity) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveWithoutId(output);
        return output.buildResult();
    }

    public static void load(Entity entity, CompoundTag tag) {
        entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), tag));
    }

    private EntityTags() {
    }
}
