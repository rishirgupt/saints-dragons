package com.leon.saintsdragons.common.registry;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.block.RaevyxEggBlockEntity;
import com.leon.saintsdragons.common.block.IgnivorusEggBlockEntity;
import com.leon.saintsdragons.common.block.CindervaneEggBlockEntity;
import com.leon.saintsdragons.common.block.VarasuchusEggBlockEntity;
import com.leon.saintsdragons.common.block.StegonautEggBlockEntity;
import com.leon.saintsdragons.common.block.VolitansEggBlockEntity;
import com.leon.saintsdragons.common.block.AtroxiiaEggBlockEntity;
import com.leon.saintsdragons.common.block.DraconianNucleusBlockEntity;
import com.leon.saintsdragons.common.block.DraconicCrucibleBlockEntity;
import com.leon.saintsdragons.platform.RegistryHelper;
import com.leon.saintsdragons.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class ModBlockEntities {
    private static final RegistryHelper.RegistryWrapper<BlockEntityType<?>> REGISTER =
            Services.PLATFORM.getRegistryHelper()
                    .create(Registries.BLOCK_ENTITY_TYPE, () -> BuiltInRegistries.BLOCK_ENTITY_TYPE, SaintsDragonsCommon.MOD_ID);

    public static final Supplier<BlockEntityType<RaevyxEggBlockEntity>> RAEVYX_EGG =
            REGISTER.register("raevyx_egg", () -> new BlockEntityType<>(RaevyxEggBlockEntity::new, java.util.Set.of(ModBlocks.RAEVYX_EGG.get())));

    public static final Supplier<BlockEntityType<IgnivorusEggBlockEntity>> IGNIVORUS_EGG =
            REGISTER.register("ignivorus_egg", () -> new BlockEntityType<>(IgnivorusEggBlockEntity::new, java.util.Set.of(ModBlocks.IGNIVORUS_EGG.get())));

    public static final Supplier<BlockEntityType<CindervaneEggBlockEntity>> CINDERVANE_EGG =
            REGISTER.register("cindervane_egg", () -> new BlockEntityType<>(CindervaneEggBlockEntity::new, java.util.Set.of(ModBlocks.CINDERVANE_EGG.get())));

    public static final Supplier<BlockEntityType<VarasuchusEggBlockEntity>> VARASUCHUS_EGG =
            REGISTER.register("varasuchus_egg", () -> new BlockEntityType<>(VarasuchusEggBlockEntity::new, java.util.Set.of(ModBlocks.VARASUCHUS_EGG.get())));

    public static final Supplier<BlockEntityType<StegonautEggBlockEntity>> STEGONAUT_EGG =
            REGISTER.register("stegonaut_egg", () -> new BlockEntityType<>(StegonautEggBlockEntity::new, java.util.Set.of(ModBlocks.STEGONAUT_EGG.get())));

    public static final Supplier<BlockEntityType<VolitansEggBlockEntity>> VOLITANS_EGG =
            REGISTER.register("volitans_egg", () -> new BlockEntityType<>(VolitansEggBlockEntity::new, java.util.Set.of(ModBlocks.VOLITANS_EGG.get())));

    public static final Supplier<BlockEntityType<AtroxiiaEggBlockEntity>> ATROXIIA_EGG =
            REGISTER.register("atroxiia_egg", () -> new BlockEntityType<>(AtroxiiaEggBlockEntity::new, java.util.Set.of(ModBlocks.ATROXIIA_EGG.get())));

    public static final Supplier<BlockEntityType<DraconianNucleusBlockEntity>> DRACONIAN_NUCLEUS =
            REGISTER.register("draconian_nucleus", () -> new BlockEntityType<>(DraconianNucleusBlockEntity::new, java.util.Set.of(ModBlocks.DRACONIAN_NUCLEUS.get())));

    public static final Supplier<BlockEntityType<DraconicCrucibleBlockEntity>> DRACONIC_CRUCIBLE =
            REGISTER.register("draconic_crucible", () -> new BlockEntityType<>(DraconicCrucibleBlockEntity::new, java.util.Set.of(ModBlocks.DRACONIC_CRUCIBLE.get())));

    public static void register() {
        REGISTER.register();
    }
}
