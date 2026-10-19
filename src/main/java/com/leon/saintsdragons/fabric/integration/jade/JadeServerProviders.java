package com.leon.saintsdragons.fabric.integration.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Server halves of the Jade providers. Jade no longer allows one object to be both the server data
 * provider and the client tooltip component, so these delegate to the shared logic in the client enums.
 */
public final class JadeServerProviders {
    public enum Dragon implements IServerDataProvider<EntityAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
            DragonEntityProvider.INSTANCE.appendServerData(tag, accessor);
        }

        @Override
        public Identifier getUid() {
            return DragonEntityProvider.INSTANCE.getUid();
        }
    }

    public enum Egg implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
            DragonEggBlockProvider.INSTANCE.appendServerData(tag, accessor);
        }

        @Override
        public Identifier getUid() {
            return DragonEggBlockProvider.INSTANCE.getUid();
        }
    }

    public enum Crucible implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
            DraconicCrucibleBlockProvider.INSTANCE.appendServerData(tag, accessor);
        }

        @Override
        public Identifier getUid() {
            return DraconicCrucibleBlockProvider.INSTANCE.getUid();
        }
    }

    private JadeServerProviders() {
    }
}
