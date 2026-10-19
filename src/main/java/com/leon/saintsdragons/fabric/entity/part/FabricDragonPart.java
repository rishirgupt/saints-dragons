package com.leon.saintsdragons.fabric.entity.part;

import net.minecraft.network.syncher.SynchedEntityData;
import com.leon.saintsdragons.server.entity.base.DragonPartEntity;
import com.leon.saintsdragons.server.entity.dragons.ignivorus.Ignivorus;
import com.leon.saintsdragons.server.entity.part.IgnivorusHitboxes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class FabricDragonPart extends Entity implements DragonPartEntity {
    private static final AtomicInteger CLIENT_IDS = new AtomicInteger(-1);
    public final @Nullable Entity parent;
    public final String partName;
    private final int partIndex;
    private EntityDimensions size = EntityDimensions.scalable(1.0F, 1.0F);
    private float damageMultiplier = 1.0f;

    public FabricDragonPart(Entity parent, int index) {
        super(FabricPartEntities.DRAGON_PART, parent.level());
        this.parent = parent;
        this.partIndex = index;
        var region = IgnivorusHitboxes.REGIONS.get(index);
        this.partName = region.name();
        this.damageMultiplier = region.damage();
        this.size = EntityDimensions.scalable(1.0F, 1.0F);
        this.noPhysics = true;
        this.setNoGravity(true);
        if (level().isClientSide()) setId(CLIENT_IDS.getAndDecrement());
    }

    public FabricDragonPart(EntityType<? extends FabricDragonPart> type, Level level) {
        super(type, level);
        this.parent = null;
        this.partIndex = -1;
        this.partName = "unknown";
        this.size = EntityDimensions.scalable(0.0f, 0.0f);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    @Override
    public @Nullable Entity getDragonParent() {
        return parent;
    }

    @Override public int getPartIndex() { return partIndex; }

    @Override
    public void updateBounds(AABB bounds) {
        xOld = getX(); yOld = getY(); zOld = getZ();
        xo = getX(); yo = getY(); zo = getZ();
        Vec3 center = bounds.getCenter();
        setPos(center.x, bounds.minY, center.z);
        setBoundingBox(bounds);
        size = EntityDimensions.scalable((float) Math.max(bounds.getXsize(), bounds.getZsize()), (float) bounds.getYsize());
    }

    @Override
    public boolean isPickable() {
        return !isRemoved() && getDragonParent() instanceof Ignivorus dragon
                && dragon.isAlive() && !dragon.isBaby();
    }

    @Override public boolean isAlive() {
        return !isRemoved() && getDragonParent() != null && getDragonParent().isAlive();
    }

    @Override
    public Entity getRootVehicle() {
        return getDragonParent() == null ? this : getDragonParent().getRootVehicle();
    }

    @Override
    public boolean startRiding(@NotNull Entity entity, boolean force, boolean sendEventAndTriggers) {
        return false;
    }

    @Override
    public void addPassenger(@NotNull Entity passenger) {
        if (parent != null) {
            passenger.startRiding(parent, true, true);
            return;
        }
        super.addPassenger(passenger);
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger) {
        return false;
    }

    @Override
    public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 location) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel hurtLevel, @NotNull DamageSource source, float amount) {
        return isPickable() && hurtParent(source, amount);
    }

    @Override
    public boolean is(@NotNull Entity entity) {
        return this == entity || (this.parent != null && this.parent == entity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    
    protected void readAdditionalSaveDataTag(@NotNull CompoundTag tag) {
    }

    
    protected void addAdditionalSaveDataTag(@NotNull CompoundTag tag) {
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return this.size;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity serverEntity) {
        return null;
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.parent == null
            || this.parent.isRemoved()
            || this.parent.level() != this.level()) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public boolean isPushable() {
        return false;
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
