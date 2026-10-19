package com.leon.saintsdragons.server.entity.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class VisualFallingBlockEntity extends Entity {
    private static final EntityDataAccessor<BlockState> BLOCK_STATE =
            SynchedEntityData.defineId(VisualFallingBlockEntity.class, EntityDataSerializers.BLOCK_STATE);

    private int lifetime = 40;

    public VisualFallingBlockEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    public VisualFallingBlockEntity(EntityType<?> entityType, Level level, double x, double y, double z, BlockState blockState, int lifetime) {
        this(entityType, level);
        this.setBlockState(blockState);
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.lifetime = lifetime;
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BLOCK_STATE, Blocks.AIR.defaultBlockState());
    }

    public BlockState getBlockState() {
        return this.entityData.get(BLOCK_STATE);
    }

    public void setBlockState(BlockState blockState) {
        this.entityData.set(BLOCK_STATE, blockState);
    }

    @Override
    public void tick() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        if (this.onGround() && this.tickCount > 20) {
            this.discard();
        }
        if (this.tickCount > lifetime) {
            this.discard();
        }
    }

    
    protected void addAdditionalSaveDataTag(CompoundTag tag) {
        tag.put("BlockState", NbtUtils.writeBlockState(getBlockState()));
        tag.putInt("Lifetime", this.lifetime);
    }

    
    protected void readAdditionalSaveDataTag(CompoundTag tag) {
        this.setBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), tag.getCompoundOrEmpty("BlockState")));
        this.lifetime = tag.getIntOr("Lifetime", 0);
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
    }

    // 26.x save bridge: mod data is kept in one compound so the tag-based save code stays unchanged.
    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        CompoundTag modData = new CompoundTag();
        this.addAdditionalSaveDataTag(modData);
        output.store("SaintsDragonsData", CompoundTag.CODEC, modData);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        this.readAdditionalSaveDataTag(input.read("SaintsDragonsData", CompoundTag.CODEC).orElseGet(CompoundTag::new));
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }
}
