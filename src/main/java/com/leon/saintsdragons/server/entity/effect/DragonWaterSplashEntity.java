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

public class DragonWaterSplashEntity extends Entity {
    private static final int FRAME_TIME = 3;
    private static final int FRAME_COUNT = 4;
    private static final int DURATION = FRAME_TIME * FRAME_COUNT;
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(DragonWaterSplashEntity.class, EntityDataSerializers.FLOAT);
    private int age;

    public DragonWaterSplashEntity(EntityType<? extends DragonWaterSplashEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public DragonWaterSplashEntity(Level level, Vec3 position, float yaw, float scale) {
        this(ModEntities.DRAGON_WATER_SPLASH.get(), level);
        setPos(position);
        setYRot(yaw);
        this.yRotO = yaw;
        setScale(scale);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SCALE, 1.0F);
    }

    public void setScale(float scale) {
        this.entityData.set(DATA_SCALE, Math.max(0.35F, scale));
    }

    public float getScale(float partialTicks) {
        float progress = Math.min((age + partialTicks) / 5.0F, 1.0F);
        return this.entityData.get(DATA_SCALE) * (0.65F + progress * 0.35F);
    }

    public float getOpacity(float partialTicks) {
        float ageFrac = (age + partialTicks) / (float) DURATION;
        return Math.max(0.75F * (1.0F - ageFrac * ageFrac), 0.0F);
    }

    public int getAnimationFrame(float partialTicks) {
        return Math.min((int) ((age + partialTicks) / FRAME_TIME), FRAME_COUNT - 1);
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (!level().isClientSide() && age >= DURATION) {
            discard();
        }
    }

    
    protected void readAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        age = tag.getIntOr("Age", 0);
        setYRot(tag.getFloatOr("Yaw", 0.0F));
        this.yRotO = getYRot();
        setScale(tag.contains("Scale") ? tag.getFloatOr("Scale", 0.0F) : 1.0F);
    }

    
    protected void addAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putFloat("Yaw", getYRot());
        tag.putFloat("Scale", this.entityData.get(DATA_SCALE));
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
