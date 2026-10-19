package com.leon.saintsdragons.platform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

/**
 * Since 1.21.2 item and block properties must carry their registry id before the object is constructed.
 * The registry wrapper publishes the id being registered here so property builders can pick it up.
 */
public final class RegistrationContext {
    @Nullable
    private static Identifier current;

    public static void begin(Identifier id) {
        current = id;
    }

    public static void end() {
        current = null;
    }

    public static Item.Properties itemProps() {
        Item.Properties properties = new Item.Properties();
        return current != null ? properties.setId(ResourceKey.create(Registries.ITEM, current)) : properties;
    }

    public static BlockBehaviour.Properties blockProps() {
        return withId(BlockBehaviour.Properties.of());
    }

    public static BlockBehaviour.Properties blockPropsCopy(Block source) {
        return withId(BlockBehaviour.Properties.ofFullCopy(source));
    }

    private static BlockBehaviour.Properties withId(BlockBehaviour.Properties properties) {
        return current != null ? properties.setId(ResourceKey.create(Registries.BLOCK, current)) : properties;
    }

    private RegistrationContext() {
    }
}
