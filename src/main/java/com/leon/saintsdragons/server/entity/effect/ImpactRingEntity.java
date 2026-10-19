package com.leon.saintsdragons.server.entity.effect;

import com.leon.saintsdragons.common.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ImpactRingEntity extends Entity {
    public static final float RENDER_PLANE_Y = 0.12F;
    private static final int DURATION = 16;
    private static final EntityDataAccessor<Float> DATA_VISUAL_SCALE =
            SynchedEntityData.defineId(ImpactRingEntity.class, EntityDataSerializers.FLOAT);
    private int age;

    public ImpactRingEntity(EntityType<? extends ImpactRingEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public ImpactRingEntity(Level level, Vec3 position) {
        this(ModEntities.STEGONAUT_IMPACT_RING.get(), level);
        setPos(position);
        GroundEffectSurfaceSnap.snap(this, RENDER_PLANE_Y);
    }

    public ImpactRingEntity(Level level, Vec3 position, float visualScale) {
        this(level, position);
        setVisualScale(visualScale);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_VISUAL_SCALE, 1.0F);
    }

    public void setVisualScale(float visualScale) {
        entityData.set(DATA_VISUAL_SCALE, Math.max(0.05F, visualScale));
    }

    public int getAge() {
        return age;
    }

    public int getDuration() {
        return DURATION;
    }

    public float getScale(float partialTicks) {
        float ageFrac = (age + partialTicks) / (float) DURATION;
        return (0.8F + ageFrac * 3.8F) * entityData.get(DATA_VISUAL_SCALE);
    }

    public float getOpacity(float partialTicks) {
        float ageFrac = (age + partialTicks) / (float) DURATION;
        return Math.max(1.0F - ageFrac * ageFrac, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        GroundEffectSurfaceSnap.snap(this, RENDER_PLANE_Y);
        if (!level().isClientSide() && age >= DURATION) {
            discard();
        }
    }

    
    protected void readAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        age = tag.getIntOr("Age", 0);
        setVisualScale(tag.contains("VisualScale") ? tag.getFloatOr("VisualScale", 0.0F) : 1.0F);
    }

    
    protected void addAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putFloat("VisualScale", entityData.get(DATA_VISUAL_SCALE));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
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
