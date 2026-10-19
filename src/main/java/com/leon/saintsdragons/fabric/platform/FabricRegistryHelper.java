package com.leon.saintsdragons.fabric.platform;

import com.leon.saintsdragons.platform.RegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public final class FabricRegistryHelper implements RegistryHelper {
    @Override
    public <T> RegistryWrapper<T> create(ResourceKey<? extends Registry<T>> registryKey,
                                         Supplier<Registry<T>> backingRegistry,
                                         String modId) {
        Registry<T> registry = backingRegistry.get();
        return new Wrapper<>(registry, modId);
    }

    private static final class Wrapper<T> implements RegistryWrapper<T> {
        private final Registry<T> registry;
        private final String modId;

        private Wrapper(Registry<T> registry, String modId) {
            this.registry = registry;
            this.modId = modId;
        }

        @Override
        public <I extends T> Supplier<I> register(String name, Supplier<I> supplier) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, name);
            com.leon.saintsdragons.platform.RegistrationContext.begin(id);
            I value;
            try {
                value = supplier.get();
            } finally {
                com.leon.saintsdragons.platform.RegistrationContext.end();
            }
            Registry.register(registry, id, value);
            return () -> value;
        }

        @Override
        public void register() {
            // Fabric registers immediately, nothing to do.
        }
    }
}
