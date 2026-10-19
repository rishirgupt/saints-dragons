package com.leon.saintsdragons.common.block;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.config.dragon.DragonAttributeConfigLoader;
import com.leon.saintsdragons.common.registry.ModBlockEntities;
import com.leon.saintsdragons.common.registry.ModEntities;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.leon.saintsdragons.server.entity.dragons.varasuchus.Varasuchus;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class VarasuchusEggBlock extends AbstractTimedDragonEggBlock<VarasuchusEggBlockEntity> {
    private static final int DEFAULT_HATCH_TICKS = 24000; // 20 minutes
    private static final VoxelShape SHAPE = box(4.0D, 0.0D, 4.0D, 12.0D, 10.0D, 12.0D);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.BaseEntityBlock> codec() {
        return simpleCodec(VarasuchusEggBlock::new);
    }

    public VarasuchusEggBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0));
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected Identifier getDragonConfigId() {
        return DragonAttributeConfigLoader.VARASUCHUS_ID;
    }

    @Override
    protected int getDefaultNormalHatchTicks() {
        return DEFAULT_HATCH_TICKS;
    }

    @Override
    protected Supplier<BlockEntityType<VarasuchusEggBlockEntity>> getEggBlockEntityType() {
        return ModBlockEntities.VARASUCHUS_EGG;
    }

    @Override
    protected VarasuchusEggBlockEntity createEggBlockEntity(BlockPos pos, BlockState state) {
        return new VarasuchusEggBlockEntity(pos, state);
    }

    @Override
    protected DragonEntity createBaby(ServerLevel level) {
        return ModEntities.VARASUCHUS.get().create(level, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
    }

    @Override
    protected void applyBabyAttributes(DragonEntity baby) {
        ((Varasuchus) baby).applyConfiguredAttributes();
    }

    @Override
    protected void configureHatchedBabyVariant(ServerLevel level,
                                               BlockPos pos,
                                               VarasuchusEggBlockEntity eggEntity,
                                               DragonEntity baby) {
        if (Varasuchus.shouldUseVoidKissedVariant(level)) {
            baby.setPendingAdultTextureVariantId(Varasuchus.VOID_KISSED_VARIANT_ID);
        }
    }

    @Override
    protected Identifier getHatchAdvancementId() {
        return SaintsDragonsCommon.rl("hatch_varasuchus");
    }
}
