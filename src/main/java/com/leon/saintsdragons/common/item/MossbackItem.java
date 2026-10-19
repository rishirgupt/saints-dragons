package com.leon.saintsdragons.common.item;

import com.leon.saintsdragons.common.registry.ModEntities;
import com.leon.saintsdragons.server.entity.dragons.Mossback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.constant.DataTickets;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MossbackItem extends Item implements GeoItem {
    private static final String BABY_TAG = "BabyMossback";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.mossback.idle");
    private static final RawAnimation BABY_IDLE = RawAnimation.begin().thenLoop("baby_mossback.animation.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MossbackItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            Mossback mossback = ModEntities.MOSSBACK.get().create(serverLevel, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
            if (mossback != null) {
                mossback.setBaby(isBaby(stack));
                Vec3 look = player.getLookAngle();
                Vec3 spawn = player.getEyePosition().add(look.scale(0.65D));
                mossback.snapTo(spawn.x, spawn.y - 0.25D, spawn.z, player.getYRot(), 0.0F);
                mossback.setDeltaMovement(look.scale(1.25D).add(0.0D, 0.18D, 0.0D));
                mossback.markThrown();
                serverLevel.addFreshEntity(mossback);
                player.playSound(SoundEvents.SNOWBALL_THROW, 0.6F, 0.85F + player.getRandom().nextFloat() * 0.25F);
                stack.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static final com.geckolib.constant.dataticket.DataTicket<Boolean> BABY =
            com.geckolib.constant.dataticket.DataTicket.create("saintsdragons_mossback_baby", Boolean.class);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new com.leon.saintsdragons.util.animation.ReleasingAnimationController<>("idle", 4, state -> {
            // The item renderer publishes whether the rendered stack is the baby variant.
            state.setAndContinue(state.getDataOrDefault(BABY, false) ? BABY_IDLE : IDLE);
            return PlayState.CONTINUE;
        }));
    }

    public static boolean isBaby(ItemStack stack) {
        return stack != null && com.leon.saintsdragons.common.item.util.StackData.getOrCreate(stack).getBooleanOr(BABY_TAG, false);
    }

    public static void setBaby(ItemStack stack, boolean baby) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (baby) {
            com.leon.saintsdragons.common.item.util.StackData.getOrCreate(stack).putBoolean(BABY_TAG, true);
        } else if (com.leon.saintsdragons.common.item.util.StackData.has(stack)) {
            com.leon.saintsdragons.common.item.util.StackData.get(stack).remove(BABY_TAG);
            if (com.leon.saintsdragons.common.item.util.StackData.get(stack).isEmpty()) {
                com.leon.saintsdragons.common.item.util.StackData.set(stack, null);
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }


    @Override
    public void createGeoRenderer(java.util.function.Consumer<com.geckolib.animatable.client.GeoRenderProvider> consumer) {
        consumer.accept(com.leon.saintsdragons.client.renderer.armor.ItemRenderProviders.mossback());
    }
}
