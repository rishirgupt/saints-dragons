package com.leon.saintsdragons.server.entity.effect;

import com.leon.saintsdragons.common.registry.ModEntities;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class LightningVisualEntity extends Entity {
    private static final EntityDataAccessor<Rotations> START_OFFSET =
            SynchedEntityData.defineId(LightningVisualEntity.class, EntityDataSerializers.ROTATIONS);
    private static final EntityDataAccessor<Rotations> END_OFFSET =
            SynchedEntityData.defineId(LightningVisualEntity.class, EntityDataSerializers.ROTATIONS);
    private static final EntityDataAccessor<Float> VISUAL_SCALE =
            SynchedEntityData.defineId(LightningVisualEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> MAX_AGE =
            SynchedEntityData.defineId(LightningVisualEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RENDER_SEED =
            SynchedEntityData.defineId(LightningVisualEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VISUAL_STYLE =
            SynchedEntityData.defineId(LightningVisualEntity.class, EntityDataSerializers.INT);

    public LightningVisualEntity(EntityType<? extends LightningVisualEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = false;
        this.noPhysics = true;
    }

    public LightningVisualEntity(Level level, Vec3 start, Vec3 end, float visualScale, int maxAge, long seed) {
        this(level, start, end, visualScale, maxAge, seed, VisualStyle.RAEVYX);
    }

    public LightningVisualEntity(Level level, Vec3 start, Vec3 end, float visualScale,
                                 int maxAge, long seed, VisualStyle visualStyle) {
        this(ModEntities.RAEVYX_GROUND_REND_TRAIL.get(), level);
        Vec3 midpoint = start.lerp(end, 0.5D);
        this.setPos(midpoint.x, midpoint.y, midpoint.z);
        this.xo = midpoint.x;
        this.yo = midpoint.y;
        this.zo = midpoint.z;
        this.setSegment(start.subtract(midpoint), end.subtract(midpoint));
        this.setVisualScale(visualScale);
        this.setMaxAge(maxAge);
        this.setRenderSeed((int)(seed ^ (seed >>> 32)));
        this.setVisualStyle(visualStyle);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(START_OFFSET, new Rotations(0.0F, 0.0F, 0.0F));
        builder.define(END_OFFSET, new Rotations(0.0F, 0.0F, 0.0F));
        builder.define(VISUAL_SCALE, 0.5F);
        builder.define(MAX_AGE, 4);
        builder.define(RENDER_SEED, 0);
        builder.define(VISUAL_STYLE, VisualStyle.RAEVYX.ordinal());
    }

    public void setSegment(Vec3 startOffset, Vec3 endOffset) {
        this.entityData.set(START_OFFSET, toRotations(startOffset));
        this.entityData.set(END_OFFSET, toRotations(endOffset));
    }

    public Vec3 getStartOffset() {
        return fromRotations(this.entityData.get(START_OFFSET));
    }

    public Vec3 getEndOffset() {
        return fromRotations(this.entityData.get(END_OFFSET));
    }

    public float getVisualScale() {
        return this.entityData.get(VISUAL_SCALE);
    }

    public void setVisualScale(float visualScale) {
        this.entityData.set(VISUAL_SCALE, Mth.clamp(visualScale, 0.1F, 2.0F));
    }

    public int getMaxAge() {
        return this.entityData.get(MAX_AGE);
    }

    public void setMaxAge(int maxAge) {
        this.entityData.set(MAX_AGE, Math.max(1, maxAge));
    }

    public int getRenderSeed() {
        return this.entityData.get(RENDER_SEED);
    }

    public void setRenderSeed(int renderSeed) {
        this.entityData.set(RENDER_SEED, renderSeed);
    }

    public VisualStyle getVisualStyle() {
        return VisualStyle.byId(this.entityData.get(VISUAL_STYLE));
    }

    public void setVisualStyle(VisualStyle visualStyle) {
        this.entityData.set(VISUAL_STYLE,
                visualStyle == null ? VisualStyle.RAEVYX.ordinal() : visualStyle.ordinal());
    }

    public float getRenderAlpha(float partialTick) {
        float age = this.tickCount + partialTick;
        float fadeWindow = Math.min(1.5F, Math.max(0.75F, this.getMaxAge() * 0.4F));
        float fadeOut = (this.getMaxAge() - age) / fadeWindow;
        return Mth.clamp(fadeOut, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);
        if (this.tickCount >= this.getMaxAge()) {
            this.discard();
        }
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double limit = 128.0D * getViewScale();
        return distance < limit * limit;
    }

    
    protected void addAdditionalSaveDataTag(CompoundTag tag) {
        Vec3 startOffset = this.getStartOffset();
        Vec3 endOffset = this.getEndOffset();
        tag.putFloat("StartX", (float)startOffset.x);
        tag.putFloat("StartY", (float)startOffset.y);
        tag.putFloat("StartZ", (float)startOffset.z);
        tag.putFloat("EndX", (float)endOffset.x);
        tag.putFloat("EndY", (float)endOffset.y);
        tag.putFloat("EndZ", (float)endOffset.z);
        tag.putFloat("Scale", this.getVisualScale());
        tag.putInt("MaxAge", this.getMaxAge());
        tag.putInt("Seed", this.getRenderSeed());
        tag.putInt("VisualStyle", this.getVisualStyle().ordinal());
    }

    
    protected void readAdditionalSaveDataTag(CompoundTag tag) {
        this.setSegment(
                new Vec3(tag.getFloatOr("StartX", 0.0F), tag.getFloatOr("StartY", 0.0F), tag.getFloatOr("StartZ", 0.0F)),
                new Vec3(tag.getFloatOr("EndX", 0.0F), tag.getFloatOr("EndY", 0.0F), tag.getFloatOr("EndZ", 0.0F))
        );
        this.setVisualScale(tag.getFloatOr("Scale", 0.0F));
        this.setMaxAge(tag.getIntOr("MaxAge", 0));
        this.setRenderSeed(tag.getIntOr("Seed", 0));
        this.setVisualStyle(VisualStyle.byId(tag.getIntOr("VisualStyle", 0)));
        this.noPhysics = true;
    }

    private static Rotations toRotations(Vec3 vec) {
        return new Rotations((float)vec.x, (float)vec.y, (float)vec.z);
    }

    private static Vec3 fromRotations(Rotations rotations) {
        return new Vec3(rotations.x(), rotations.y(), rotations.z());
    }

    public enum VisualStyle {
        RAEVYX,
        BLOOD_TEMPEST,
        BLOOD_TEMPEST_SLASH,
        BLOOD_TEMPEST_STORM;

        private static VisualStyle byId(int id) {
            VisualStyle[] values = values();
            return id >= 0 && id < values.length ? values[id] : RAEVYX;
        }
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
