package com.leon.saintsdragons.server.entity.effect.ignivorus;

import net.minecraft.network.syncher.SynchedEntityData;
import com.leon.saintsdragons.common.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class IgnivorusSkyfallRingEntity extends Entity {

    private static final int DURATION = 20;

    private int age;

    public IgnivorusSkyfallRingEntity(EntityType<? extends IgnivorusSkyfallRingEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public IgnivorusSkyfallRingEntity(Level level, Vec3 position) {
        this(ModEntities.IGNIVORUS_NOVA_RING.get(), level);
        setPos(position);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    public int getAge() {
        return this.age;
    }

    public int getDuration() {
        return DURATION;
    }

    public float getScale(float partialTicks) {
        float ageFrac = (this.age + partialTicks) / (float) DURATION;
        return ageFrac * 5.0F;
    }

    public float getOpacity(float partialTicks) {
        float ageFrac = (this.age + partialTicks) / (float) DURATION;
        return (float) Math.max((1.0 - ageFrac * ageFrac) * 0.8, 0.0);
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;

        if (!this.level().isClientSide()) {
            if (this.age >= DURATION) {
                this.discard();
            }
        }
    }

    
    protected void readAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        this.age = tag.getIntOr("Age", 0);
    }

    
    protected void addAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        tag.putInt("Age", this.age);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0;
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
