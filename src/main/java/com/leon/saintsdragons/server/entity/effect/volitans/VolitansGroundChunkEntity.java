package com.leon.saintsdragons.server.entity.effect.volitans;

import com.leon.saintsdragons.common.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

public class VolitansGroundChunkEntity extends Entity implements GeoEntity {
    private static final RawAnimation SPAWN =
            RawAnimation.begin().thenPlay("animation.ground_chunk.spawn");
    private static final RawAnimation DESPAWN =
            RawAnimation.begin().thenPlay("animation.ground_chunk.despawn");
    private static final EntityDimensions DIMENSIONS = EntityDimensions.fixed(9.0F, 2.0F);
    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE =
            SynchedEntityData.defineId(VolitansGroundChunkEntity.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Boolean> DATA_DESPAWNING =
            SynchedEntityData.defineId(VolitansGroundChunkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_VISUAL_YAW =
            SynchedEntityData.defineId(VolitansGroundChunkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_READY =
            SynchedEntityData.defineId(VolitansGroundChunkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int HOLD_TICKS = 18;
    private static final int DESPAWN_TICKS = 9;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int despawnTicks;

    public VolitansGroundChunkEntity(EntityType<? extends VolitansGroundChunkEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public VolitansGroundChunkEntity(Level level, Vec3 pos, float yaw, BlockState blockState) {
        this(ModEntities.VOLITANS_GROUND_CHUNK.get(), level);
        setPos(pos);
        setBlockState(blockState);
        initializeRotation(yaw);
        setReady(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK_STATE, Blocks.DIRT.defaultBlockState());
        builder.define(DATA_DESPAWNING, false);
        builder.define(DATA_VISUAL_YAW, 0.0F);
        builder.define(DATA_READY, false);
    }

    public BlockState getBlockState() {
        return this.entityData.get(DATA_BLOCK_STATE);
    }

    public void setBlockState(BlockState state) {
        this.entityData.set(DATA_BLOCK_STATE, state.isAir() ? Blocks.DIRT.defaultBlockState() : state);
    }

    public boolean isDespawning() {
        return this.entityData.get(DATA_DESPAWNING);
    }

    public float getVisualYaw() {
        return this.entityData.get(DATA_VISUAL_YAW);
    }

    public boolean isReady() {
        return this.entityData.get(DATA_READY);
    }

    private void setReady(boolean ready) {
        this.entityData.set(DATA_READY, ready);
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);

        if (level().isClientSide()) {
            return;
        }
        if (tickCount >= HOLD_TICKS && !isDespawning()) {
            this.entityData.set(DATA_DESPAWNING, true);
        }
        if (isDespawning() && ++despawnTicks >= DESPAWN_TICKS) {
            discard();
        }
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel hurtLevel, @NotNull DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    
    protected void readAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        if (tag.contains("BlockState")) {
            setBlockState(NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK), tag.getCompoundOrEmpty("BlockState")));
        }
        if (tag.contains("Yaw")) {
            initializeRotation(tag.getFloatOr("Yaw", 0.0F));
        }
        if (tag.contains("VisualYaw")) {
            this.entityData.set(DATA_VISUAL_YAW, tag.getFloatOr("VisualYaw", 0.0F));
        }
        if (tag.contains("Ready")) {
            setReady(tag.getBooleanOr("Ready", false));
        }
        if (tag.getBooleanOr("Despawning", false)) {
            this.entityData.set(DATA_DESPAWNING, true);
        }
        despawnTicks = tag.getIntOr("DespawnTicks", 0);
    }

    
    protected void addAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        tag.put("BlockState", NbtUtils.writeBlockState(getBlockState()));
        tag.putFloat("Yaw", getYRot());
        tag.putFloat("VisualYaw", getVisualYaw());
        tag.putBoolean("Ready", isReady());
        tag.putBoolean("Despawning", isDespawning());
        tag.putInt("DespawnTicks", despawnTicks);
    }

    @Override
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        initializeRotation(packet.getYRot());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return DIMENSIONS.withEyeHeight(1.0F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new com.leon.saintsdragons.util.animation.ReleasingAnimationController<VolitansGroundChunkEntity>("controller", 0, this::animationPredicate));
    }

    private PlayState animationPredicate(AnimationTest<VolitansGroundChunkEntity> state) {
        state.controller().setAnimation(isDespawning() ? DESPAWN : SPAWN);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private void initializeRotation(float yaw) {
        setYRot(yaw);
        setYBodyRot(yaw);
        setYHeadRot(yaw);
        this.entityData.set(DATA_VISUAL_YAW, yaw);
        this.yRotO = yaw;
        this.xRotO = 0.0F;
    }

    public static Vec3 surfacePosition(BlockPos groundPos) {
        return Vec3.atBottomCenterOf(groundPos.above());
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
}
