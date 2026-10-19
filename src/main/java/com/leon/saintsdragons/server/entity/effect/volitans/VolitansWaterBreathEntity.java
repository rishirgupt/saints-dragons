package com.leon.saintsdragons.server.entity.effect.volitans;

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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/** Legacy sprite presentation only. VolitansBreathStream owns all gameplay collision and effects. */
public class VolitansWaterBreathEntity extends Entity {
    private static final EntityDataAccessor<Boolean> DATA_POISON_MODE =
            SynchedEntityData.defineId(VolitansWaterBreathEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_LIFETIME =
            SynchedEntityData.defineId(VolitansWaterBreathEntity.class, EntityDataSerializers.INT);
    private int age;

    public VolitansWaterBreathEntity(EntityType<? extends VolitansWaterBreathEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public VolitansWaterBreathEntity(Level level, Vec3 position, Vec3 velocity, int lifetime, boolean poison) {
        this(ModEntities.VOLITANS_WATER_BREATH.get(), level);
        setPos(position);
        setDeltaMovement(velocity);
        entityData.set(DATA_LIFETIME, Math.max(1, lifetime));
        entityData.set(DATA_POISON_MODE, poison);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_POISON_MODE, false);
        builder.define(DATA_LIFETIME, 28);
    }

    @Override
    public void tick() {
        super.tick();
        if (++age >= getMaxAge()) {
            discard();
            return;
        }
        Vec3 start = position();
        Vec3 end = start.add(getDeltaMovement());
        var hit = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            discard();
            return;
        }
        setPos(end.x, end.y, end.z);
        setDeltaMovement(getDeltaMovement().scale(0.997));
    }

    public int getAge() {
        return age;
    }

    public int getMaxAge() {
        return entityData.get(DATA_LIFETIME);
    }

    public boolean isPoisonMode() {
        return entityData.get(DATA_POISON_MODE);
    }

    
    protected void readAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        age = tag.getIntOr("Age", 0);
        entityData.set(DATA_LIFETIME, Math.max(1, tag.getIntOr("MaxAge", 0)));
        entityData.set(DATA_POISON_MODE, tag.getBooleanOr("PoisonMode", false));
    }

    
    protected void addAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putInt("MaxAge", getMaxAge());
        tag.putBoolean("PoisonMode", isPoisonMode());
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
