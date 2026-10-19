package com.leon.saintsdragons.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;

/**
 * Buffer helpers for values that lost their FriendlyByteBuf methods when packets moved to stream codecs.
 * Play-phase buffers are always registry-aware, which item stacks need.
 */
public final class NetBuf {
    public static void writeComponent(FriendlyByteBuf buffer, Component component) {
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buffer, component);
    }

    public static Component readComponent(FriendlyByteBuf buffer) {
        return ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buffer);
    }

    public static void writeItem(FriendlyByteBuf buffer, ItemStack stack) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryAware(buffer), stack);
    }

    public static ItemStack readItem(FriendlyByteBuf buffer) {
        return ItemStack.OPTIONAL_STREAM_CODEC.decode(registryAware(buffer));
    }

    private static RegistryFriendlyByteBuf registryAware(FriendlyByteBuf buffer) {
        if (buffer instanceof RegistryFriendlyByteBuf registryBuffer) {
            return registryBuffer;
        }
        throw new IllegalStateException("Item stacks can only be sent in the play phase");
    }

    private NetBuf() {
    }
}
