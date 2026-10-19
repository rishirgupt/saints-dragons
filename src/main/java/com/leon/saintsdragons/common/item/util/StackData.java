package com.leon.saintsdragons.common.item.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Item stacks no longer carry a raw NBT tag (1.20.5+). The mod's per-stack data lives in the
 * {@code minecraft:custom_data} component; these helpers keep the old read/modify/write shape.
 * Tags returned here are copies: call {@link #set} or {@link #update} to store changes.
 */
public final class StackData {
    public static boolean has(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    @Nullable
    public static CompoundTag get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null || data.isEmpty() ? null : data.copyTag();
    }

    public static CompoundTag getOrCreate(ItemStack stack) {
        CompoundTag tag = get(stack);
        return tag != null ? tag : new CompoundTag();
    }

    public static void set(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> change) {
        CompoundTag tag = getOrCreate(stack);
        change.accept(tag);
        set(stack, tag);
    }

    /** Serializes a whole stack (item, count, components). Empty stacks become an empty compound. */
    public static CompoundTag saveStack(ItemStack stack, HolderLookup.Provider registries) {
        if (stack.isEmpty()) {
            return new CompoundTag();
        }
        Tag encoded = ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
                .result().orElse(null);
        return encoded instanceof CompoundTag compound ? compound : new CompoundTag();
    }

    public static ItemStack loadStack(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
                .result().orElse(ItemStack.EMPTY);
    }

    private StackData() {
    }
}
