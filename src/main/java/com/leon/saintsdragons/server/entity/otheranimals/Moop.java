package com.leon.saintsdragons.server.entity.otheranimals;

import com.leon.saintsdragons.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animation.AnimationController;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

public class Moop extends AbstractFish implements GeoEntity {
    private static final String MOVEMENT_CONTROLLER = "movement";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.moop.idle");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.moop.swim");
    private static final RawAnimation SWIM_FAST = RawAnimation.begin().thenLoop("animation.moop.swim_fast");
    private static final RawAnimation ON_LAND = RawAnimation.begin().thenLoop("animation.moop.on_land");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public Moop(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.8D);
    }

    public static boolean canSpawnHere(EntityType<Moop> type,
                                       LevelAccessor level,
                                       EntitySpawnReason spawnType,
                                       BlockPos pos,
                                       RandomSource random) {
        return WaterAnimal.checkSurfaceWaterAnimalSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RandomSwimmingGoal(this, 1.0D, 30));
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.25F));
    }

    @Override
    protected @NotNull SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(ModItems.BUCKET_OF_MOOP.get());
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel lootLevel, @NotNull DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(lootLevel, damageSource, recentlyHit);
        this.spawnAtLocation((net.minecraft.server.level.ServerLevel) this.level(), this.isOnFire() || this.getRemainingFireTicks() > 0
                ? ModItems.COOKED_MOOP.get()
                : ModItems.RAW_MOOP.get());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new com.leon.saintsdragons.util.animation.ReleasingAnimationController<Moop>(MOVEMENT_CONTROLLER, 4, this::movementPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    private PlayState movementPredicate(AnimationTest<Moop> state) {
        if (!isInWater()) {
            state.setAndContinue(ON_LAND);
            return PlayState.CONTINUE;
        }

        double speed = getDeltaMovement().horizontalDistanceSqr();
        if (speed > 0.01D) {
            state.setAndContinue(SWIM_FAST);
        } else if (speed > 0.0004D) {
            state.setAndContinue(SWIM);
        } else {
            state.setAndContinue(IDLE);
        }

        return PlayState.CONTINUE;
    }
}
