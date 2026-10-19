package com.leon.saintsdragons.server.entity.effect.volitans;

import com.leon.saintsdragons.common.registry.ModEntities;
import net.minecraft.core.UUIDUtil;
import com.leon.saintsdragons.common.registry.ModParticles;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.leon.saintsdragons.server.entity.dragons.volitans.Volitans;
import com.leon.saintsdragons.server.entity.dragons.util.DragonElementalImmunity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class VolitansPoisonOrbEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache animationCache =
           GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new com.leon.saintsdragons.util.animation.ReleasingAnimationController<VolitansPoisonOrbEntity>("orb", 0,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("spin"))));
    }

    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(VolitansPoisonOrbEntity.class, EntityDataSerializers.FLOAT);

    private UUID ownerUUID;
    private LivingEntity owner;
    private double impactRadius;
    private float impactDamage;
    private int poisonDurationTicks;
    private int poisonAmplifier;
    private int lifetimeTicks;
    private int livedTicks;

    public VolitansPoisonOrbEntity(EntityType<? extends VolitansPoisonOrbEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
        this.noPhysics = true;
        this.refreshDimensions();
    }

    public VolitansPoisonOrbEntity(Level level, Vec3 pos, @Nullable LivingEntity owner,
                                   double impactRadius, float impactDamage,
                                   int poisonDurationTicks, int poisonAmplifier, int lifetimeTicks) {
        this(ModEntities.VOLITANS_POISON_BALL.get(), level);
        this.setPos(pos);
        this.owner = owner;
        this.ownerUUID = owner != null ? owner.getUUID() : null;
        this.impactRadius = impactRadius;
        this.impactDamage = impactDamage;
        this.poisonDurationTicks = poisonDurationTicks;
        this.poisonAmplifier = poisonAmplifier;
        this.lifetimeTicks = lifetimeTicks;
        this.setDeltaMovement(Vec3.ZERO);
        this.setVisualScale(1.0F);
        this.needsSync = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SCALE, 1.0F);
    }

    public void setVisualScale(float scale) {
        this.entityData.set(DATA_SCALE, scale);
        this.refreshDimensions();
    }

    public float getVisualScale() {
        return this.entityData.get(DATA_SCALE);
    }

    @Override
    public void tick() {
        super.tick();
        livedTicks++;

        Vec3 currentPos = this.position();
        Vec3 motion = this.getDeltaMovement();

        if (!this.isNoGravity()) {
            float gravityProgress = Math.min(1.0F, livedTicks / 20.0F);
            double gravity = -0.05D * gravityProgress;
            motion = motion.add(0.0D, gravity, 0.0D);
        }

        Vec3 nextPos = currentPos.add(motion);

        BlockHitResult blockHit = level().clip(new ClipContext(
                currentPos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this
        ));

        boolean hitBlock = blockHit.getType() == HitResult.Type.BLOCK;
        EntityHitResult entityHit = findEntityHit(currentPos, nextPos);
        boolean hitEntity = entityHit != null;

        if (hitEntity && hitBlock) {
            double blockDist = blockHit.getLocation().distanceToSqr(currentPos);
            double entityDist = entityHit.getLocation().distanceToSqr(currentPos);
            this.setPos(entityDist <= blockDist ? entityHit.getLocation() : blockHit.getLocation());
        } else if (hitEntity) {
            this.setPos(entityHit.getLocation());
        } else if (hitBlock) {
            this.setPos(blockHit.getLocation());
        } else {
            this.setPos(nextPos);
        }

        if (!level().isClientSide()) {
            if (!this.isNoGravity()) {
                motion = motion.scale(0.99D);
            }
            this.setDeltaMovement(motion);

            if (livedTicks > lifetimeTicks || hitEntity || hitBlock) {
                explode();
            }
        } else {
            if (!this.isNoGravity()) {
                motion = motion.scale(0.99D);
            }
            this.setDeltaMovement(motion);
            spawnTrailParticles(currentPos);
        }
    }

    @Nullable
    private EntityHitResult findEntityHit(Vec3 start, Vec3 end) {
        if (level().isClientSide()) {
            return null;
        }
        AABB bounds = getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D);
        LivingEntity resolvedOwner = getOwner();
        return com.leon.saintsdragons.server.entity.effect.EffectHits.entityHit(level(), this, start, end, bounds, target -> {
            if (!(target instanceof LivingEntity living)) {
                return false;
            }
            if (!living.isAlive()) {
                return false;
            }
            if (resolvedOwner != null && (target == resolvedOwner || isAlliedTarget(resolvedOwner, living))) {
                return false;
            }
            return true;
        });
    }

    private void spawnTrailParticles(Vec3 previous) {
        Vec3 travel = position().subtract(previous);
        int samples = 2 * Math.min(24, Math.max(3, (int) Math.ceil(travel.length() / 0.25)));
        for (int i = 0; i < samples; i++) {
            Vec3 point = previous.add(travel.scale((i + random.nextDouble()) / samples));
            double spread = 0.18 * getVisualScale();
            if (i % 2 == 0) {
                level().addParticle(ModParticles.VOLITANS_POISON_ORB_EMITTER.get(), true, true,
                        point.x + (random.nextDouble() - 0.5) * spread,
                        point.y + (random.nextDouble() - 0.5) * spread,
                        point.z + (random.nextDouble() - 0.5) * spread,
                        (random.nextDouble() - 0.5) * 0.15, random.nextDouble() * 0.1,
                        (random.nextDouble() - 0.5) * 0.15);
            }
            level().addParticle(ModParticles.VOLITANS_POISON_ORB_TRAIL.get(), true, true,
                    point.x + (random.nextDouble() - 0.5) * spread,
                    point.y + (random.nextDouble() - 0.5) * spread,
                    point.z + (random.nextDouble() - 0.5) * spread,
                    (random.nextDouble() - 0.5) * 0.04, 0.025,
                    (random.nextDouble() - 0.5) * 0.04);
        }
    }

    private void explode() {
        if (!(level() instanceof ServerLevel server)) {
            discard();
            return;
        }

        Vec3 impact = position();
        float scale = getVisualScale();

        var groundHit = server.clip(new ClipContext(impact.add(0, 0.5D, 0), impact.add(0, -6.0D, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        for (var viewer : server.players()) {
            if (viewer.distanceToSqr(impact) > 128.0D * 128.0D) continue;
            server.sendParticles(viewer, ModParticles.VOLITANS_POISON_CLOUD.get(), true, true,
                    impact.x, impact.y + 0.35D * scale, impact.z, 0, scale, 0, 0, 1.0D);
            server.sendParticles(viewer, ModParticles.VOLITANS_POISON_EXPLOSION.get(), true, true,
                    impact.x, impact.y + 0.35D * scale, impact.z, 0, scale, 0, 0, 1.0D);
            if (groundHit.getType() == HitResult.Type.BLOCK && groundHit.getDirection().getStepY() > 0) {
                Vec3 ground = groundHit.getLocation();
                server.sendParticles(viewer, ModParticles.VOLITANS_POISON_GROUND_BURST.get(), true, true,
                        ground.x, ground.y + 0.04D, ground.z, 0, scale, 0, 0, 1.0D);
            }
        }

        server.playSound(null, blockPosition(), SoundEvents.SLIME_BLOCK_BREAK, getSoundSource(), 1.0F + scale * 0.08F, 0.8F);

        LivingEntity ownerEntity = getOwner();
        boolean poisonActive = !(ownerEntity instanceof Volitans volitans && volitans.isVenomNeutralized());
        AABB area = new AABB(
                impact.x - impactRadius, impact.y - impactRadius, impact.z - impactRadius,
                impact.x + impactRadius, impact.y + impactRadius, impact.z + impactRadius
        );

        List<LivingEntity> hits = server.getEntitiesOfClass(LivingEntity.class, area, target ->
                target.isAlive()
                        && target != ownerEntity
                        && (ownerEntity == null || !isAlliedTarget(ownerEntity, target))
                        && (!poisonActive || !DragonElementalImmunity.isPoisonImmune(target))
        );

        for (LivingEntity target : hits) {
            if (ownerEntity != null) {
                target.hurt(server.damageSources().mobAttack(ownerEntity), impactDamage);
            } else {
                target.hurt(server.damageSources().magic(), impactDamage);
            }
            if (poisonActive && poisonDurationTicks > 0) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDurationTicks, poisonAmplifier));
            }

            Vec3 knockback = target.position().subtract(impact);
            if (knockback.lengthSqr() > 1.0E-6) {
                knockback = knockback.normalize().scale(0.18D * scale);
                target.push(knockback.x, 0.05D, knockback.z);
                target.needsSync = true;
            }
        }

        discard();
    }

    private boolean isAlliedTarget(LivingEntity ownerEntity, LivingEntity target) {
        if (ownerEntity == null || target == null) {
            return false;
        }
        if (ownerEntity.isAlliedTo(target)) {
            return true;
        }
        if (ownerEntity instanceof DragonEntity dragon) {
            return dragon.isAlly(target);
        }
        return false;
    }

    @Nullable
    private LivingEntity getOwner() {
        if (owner == null && ownerUUID != null && level() instanceof ServerLevel server) {
            Entity entity = server.getEntity(ownerUUID);
            if (entity instanceof LivingEntity living) {
                owner = living;
            }
        }
        return owner;
    }

    
    protected void readAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        livedTicks = tag.getIntOr("Lived", 0);
        lifetimeTicks = tag.getIntOr("Lifetime", 0);
        impactRadius = tag.getDoubleOr("ImpactRadius", 0.0D);
        impactDamage = tag.getFloatOr("ImpactDamage", 0.0F);
        poisonDurationTicks = tag.getIntOr("PoisonDuration", 0);
        poisonAmplifier = tag.getIntOr("PoisonAmplifier", 0);
        if (tag.contains("Scale")) {
            setVisualScale(tag.getFloatOr("Scale", 0.0F));
        }
        if (tag.contains("Owner")) {
            ownerUUID = tag.read("Owner", UUIDUtil.CODEC).orElse(null);
        }
    }

    
    protected void addAdditionalSaveDataTag(@NotNull CompoundTag tag) {
        tag.putInt("Lived", livedTicks);
        tag.putInt("Lifetime", lifetimeTicks);
        tag.putDouble("ImpactRadius", impactRadius);
        tag.putFloat("ImpactDamage", impactDamage);
        tag.putInt("PoisonDuration", poisonDurationTicks);
        tag.putInt("PoisonAmplifier", poisonAmplifier);
        tag.putFloat("Scale", getVisualScale());
        if (ownerUUID != null) {
            tag.store("Owner", UUIDUtil.CODEC, ownerUUID);
        }
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.setDeltaMovement(packet.getMovement());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel hurtLevel, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        return false;
    }

    @Override
    public EntityDimensions getDimensions(@NotNull Pose pose) {
        float scale = getVisualScale();
        return EntityDimensions.fixed(0.98F * scale, 0.98F * scale);
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
