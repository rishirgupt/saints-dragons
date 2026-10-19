package com.leon.saintsdragons.common.block;

import com.leon.saintsdragons.server.entity.base.DragonGender;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;


public abstract class AbstractDragonEggBlockEntity extends BlockEntity {
    private static final double LEGACY_NORMAL_HATCH_TICKS = 18000.0D;

    private double hatchProgress;
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private UUID hatchAdvancementOwnerUUID;
    @Nullable
    private DragonGender babyGender;
    private boolean pausedHatchingParticlesShown;

    protected AbstractDragonEggBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public double getHatchProgress() {
        return hatchProgress;
    }

    public void setHatchProgress(double hatchProgress) {
        this.hatchProgress = Math.max(0.0D, Math.min(1.0D, hatchProgress));
        this.setChanged();
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.setChanged();
    }

    @Nullable
    public UUID getHatchAdvancementOwnerUUID() {
        return this.hatchAdvancementOwnerUUID != null ? this.hatchAdvancementOwnerUUID : this.ownerUUID;
    }

    public void setHatchAdvancementOwnerUUID(@Nullable UUID hatchAdvancementOwnerUUID) {
        this.hatchAdvancementOwnerUUID = hatchAdvancementOwnerUUID;
        this.setChanged();
    }

    @Nullable
    public DragonGender getBabyGender() {
        return this.babyGender;
    }

    public void setBabyGender(@Nullable DragonGender gender) {
        this.babyGender = gender;
        this.setChanged();
    }

    public boolean hasShownPausedHatchingParticles() {
        return this.pausedHatchingParticlesShown;
    }

    public void setPausedHatchingParticlesShown(boolean shown) {
        if (this.pausedHatchingParticlesShown != shown) {
            this.pausedHatchingParticlesShown = shown;
            this.setChanged();
        }
    }

    protected void saveModData(CompoundTag tag) {
        tag.putDouble("HatchProgress", this.hatchProgress);
        if (this.ownerUUID != null) {
            tag.store("OwnerUUID", UUIDUtil.CODEC, this.ownerUUID);
        }
        if (this.hatchAdvancementOwnerUUID != null) {
            tag.store("HatchAdvancementOwnerUUID", UUIDUtil.CODEC, this.hatchAdvancementOwnerUUID);
        }
        if (this.babyGender != null) {
            tag.putByte("BabyGender", this.babyGender.getId());
        }
        tag.putBoolean("PausedHatchingParticlesShown", this.pausedHatchingParticlesShown);
    }

    protected void loadModData(CompoundTag tag) {
        if (tag.contains("HatchProgress")) {
            this.hatchProgress = Math.max(0.0D, Math.min(1.0D, tag.getDoubleOr("HatchProgress", 0.0D)));
        } else {
            double legacyTicks = Math.max(0, tag.getIntOr("HatchProgressTicks", 0));
            this.hatchProgress = Math.max(0.0D, Math.min(1.0D, legacyTicks / LEGACY_NORMAL_HATCH_TICKS));
        }
        if (tag.contains("OwnerUUID")) {
            this.ownerUUID = tag.read("OwnerUUID", UUIDUtil.CODEC).orElse(null);
        }
        if (tag.contains("HatchAdvancementOwnerUUID")) {
            this.hatchAdvancementOwnerUUID = tag.read("HatchAdvancementOwnerUUID", UUIDUtil.CODEC).orElse(null);
        }
        if (tag.contains("BabyGender")) {
            this.babyGender = DragonGender.fromId(tag.getByteOr("BabyGender", (byte) 0));
        }
        this.pausedHatchingParticlesShown = tag.getBooleanOr("PausedHatchingParticlesShown", false);
    }

    // 26.x save bridge: mod data is kept in one compound so the tag-based save code stays unchanged.
    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag modData = new CompoundTag();
        this.saveModData(modData);
        output.store("SaintsDragonsData", CompoundTag.CODEC, modData);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.loadModData(input.read("SaintsDragonsData", CompoundTag.CODEC).orElseGet(CompoundTag::new));
    }
}
