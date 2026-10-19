package com.leon.saintsdragons.common.item;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.minecraft.core.UUIDUtil;
import com.leon.saintsdragons.common.item.util.BinderComponentUtil;
import com.leon.saintsdragons.server.data.DragonCodexSavedData;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class AbstractDragonBinderItem<T extends DragonEntity> extends Item {

    protected AbstractDragonBinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player,
                                                           @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (!getDragonClass().isInstance(target)) {
            return InteractionResult.PASS;
        }

        T dragon = getDragonClass().cast(target);
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!dragon.isTame() || !dragon.isOwnedBy(player)) {
            player.sendOverlayMessage(Component.translatable(getNotOwnerMessageKey()));
            return InteractionResult.FAIL;
        }

        if (!canBind(dragon)) {
            player.sendOverlayMessage(Component.translatable(getCannotCaptureMessageKey()));
            return InteractionResult.FAIL;
        }

        if (BinderComponentUtil.isBound(stack)) {
            player.sendOverlayMessage(Component.translatable(getAlreadyOccupiedMessageKey()));
            return InteractionResult.FAIL;
        }

        ItemStack reboundStack = captureDragon(stack, dragon, player);
        if (reboundStack.isEmpty()) {
            return InteractionResult.FAIL;
        }

        player.setItemInHand(hand, reboundStack);
        syncPlayerInventory(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return BinderComponentUtil.isBound(stack) ? InteractionResult.PASS : super.use(level, player, hand);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return super.useOn(context);
        }

        ItemStack stack = context.getItemInHand();
        if (!BinderComponentUtil.isBound(stack)) {
            return super.useOn(context);
        }

        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clickedPos = context.getClickedPos();
        Vec3 releasePosition = new Vec3(
                clickedPos.getX() + 0.5D,
                clickedPos.getY() + 1.0D,
                clickedPos.getZ() + 0.5D
        );
        boolean released = releaseDragon(stack, player, releasePosition, false);
        if (released) {
            syncPlayerInventory(player);
        }
        return released ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    private ItemStack captureDragon(ItemStack stack, T dragon, Player player) {
        ItemStack copied = stack.copy();
        CompoundTag tag = com.leon.saintsdragons.common.item.util.StackData.getOrCreate(copied);

        tag.store(BinderComponentUtil.BOUND_DRAGON_UUID, UUIDUtil.CODEC, dragon.getUUID());
        tag.putString(BinderComponentUtil.BOUND_DRAGON_NAME, dragon.getName().getString());

        if (dragon.hasCustomName()) {
            Component customName = dragon.getCustomName();
            if (customName != null) {
                tag.putString(BinderComponentUtil.BOUND_CUSTOM_NAME, com.leon.saintsdragons.common.util.ComponentJson.toJson(customName));
            } else {
                tag.remove(BinderComponentUtil.BOUND_CUSTOM_NAME);
            }
        } else {
            tag.remove(BinderComponentUtil.BOUND_CUSTOM_NAME);
        }
        tag.putBoolean(BinderComponentUtil.IS_BOUND, true);

        LivingEntity owner = dragon.getOwner();
        if (owner instanceof Player ownerPlayer) {
            tag.store(BinderComponentUtil.BOUND_OWNER_UUID, UUIDUtil.CODEC, ownerPlayer.getUUID());
            tag.putString(BinderComponentUtil.BOUND_OWNER_NAME, ownerPlayer.getName().getString());
        } else {
            tag.remove(BinderComponentUtil.BOUND_OWNER_UUID);
            tag.remove(BinderComponentUtil.BOUND_OWNER_NAME);
        }

        prepareDragonForCapture(dragon, player);
        dragon.setBoundInBinder(true);
        CompoundTag dragonData = com.leon.saintsdragons.server.entity.handler.EntityTags.save(dragon);
        tag.put(getDragonDataKey(), dragonData);
        com.leon.saintsdragons.common.item.util.StackData.set(copied, tag);

        if (player.level() instanceof ServerLevel serverLevel) {
            DragonCodexSavedData.get(serverLevel).updateDragonBoundState(player.getUUID(), dragon.getUUID(), true);
        }

        dragon.remove(Entity.RemovalReason.DISCARDED);
        if (!dragon.isRemoved()) {
            return ItemStack.EMPTY;
        }
        onDragonCaptured(dragon, player, copied);
        awardPocketDragonAdvancement(player);
        player.sendOverlayMessage(Component.translatable(getCapturedMessageKey(), dragon.getName().getString()));
        return copied;
    }

    private void awardPocketDragonAdvancement(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        var advancement = serverPlayer.level().getServer().getAdvancements()
                .get(SaintsDragonsCommon.rl("pocket_dragon"));
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, "bind_dragon");
        }
    }

    protected final boolean releaseDragon(ItemStack stack, Player player, Vec3 position, boolean airborne) {
        CompoundTag tag = com.leon.saintsdragons.common.item.util.StackData.get(stack);
        if (tag == null || !tag.contains(BinderComponentUtil.BOUND_DRAGON_UUID)) {
            return false;
        }

        UUID ownerUUID = tag.contains(BinderComponentUtil.BOUND_OWNER_UUID) ? tag.read(BinderComponentUtil.BOUND_OWNER_UUID, UUIDUtil.CODEC).orElse(null) : null;
        if (ownerUUID != null && !player.getUUID().equals(ownerUUID)) {
            player.sendOverlayMessage(Component.translatable(getReleaseNotOwnerMessageKey()));
            return false;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        String dragonName = tag.getStringOr(BinderComponentUtil.BOUND_DRAGON_NAME, "");
        UUID originalUUID = tag.read(BinderComponentUtil.BOUND_DRAGON_UUID, UUIDUtil.CODEC).orElse(null);
        T newDragon = createDragon(serverLevel);

        if (tag.contains(getDragonDataKey())) {
            try {
                CompoundTag dragonData = tag.getCompoundOrEmpty(getDragonDataKey());
                com.leon.saintsdragons.server.entity.handler.EntityTags.load(newDragon, dragonData);
                newDragon.setBoundInBinder(false);
            } catch (Exception e) {
                player.sendOverlayMessage(Component.translatable("saintsdragons.message.binder_data_corrupted"));
                return false;
            }
        }

        newDragon.setUUID(originalUUID);
        newDragon.setPos(position.x, position.y, position.z);
        prepareDragonForRelease(newDragon, player);

        if (ownerUUID != null) {
            newDragon.setTame(true, true);
            newDragon.setOwnerUUID(ownerUUID);
        } else {
            // Do not register a Codex entry until the reconstructed entity is actually accepted by the level.
            newDragon.setTame(true, true);
            newDragon.setOwnerUUID(player.getUUID());
        }

        if (tag.contains(BinderComponentUtil.BOUND_CUSTOM_NAME)) {
            try {
                Component customName = com.leon.saintsdragons.common.util.ComponentJson.fromJson(tag.getStringOr(BinderComponentUtil.BOUND_CUSTOM_NAME, ""));
                if (customName != null) {
                    newDragon.setCustomName(customName);
                }
            } catch (Exception ignored) {
            }
        }

        if (airborne && !serverLevel.noCollision(newDragon, newDragon.getBoundingBox())) {
            player.sendOverlayMessage(Component.translatable(getReleaseFailedMessageKey(), dragonName));
            return false;
        }
        if (airborne) {
            prepareDragonForAirRelease(newDragon, player);
        }

        if (!serverLevel.addFreshEntity(newDragon)) {
            player.sendOverlayMessage(Component.translatable(getReleaseFailedMessageKey(), dragonName));
            return false;
        }

        UUID resolvedOwnerUUID = ownerUUID != null ? ownerUUID : player.getUUID();
        DragonCodexSavedData codexData = DragonCodexSavedData.get(serverLevel);
        codexData.addDragon(resolvedOwnerUUID, newDragon);
        codexData.updateDragonBoundState(resolvedOwnerUUID, originalUUID, false);
        clearBinderData(tag);
        onDragonReleased(newDragon, player, stack);
        player.sendOverlayMessage(Component.translatable(getReleasedMessageKey(), dragonName));
        return true;
    }

    protected final void syncPlayerInventory(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
            serverPlayer.inventoryMenu.broadcastChanges();
        }
    }

    protected void clearBinderData(CompoundTag tag) {
        tag.remove(BinderComponentUtil.BOUND_DRAGON_UUID);
        tag.remove(BinderComponentUtil.BOUND_DRAGON_NAME);
        tag.remove(BinderComponentUtil.BOUND_OWNER_UUID);
        tag.remove(BinderComponentUtil.BOUND_OWNER_NAME);
        tag.remove(BinderComponentUtil.BOUND_CUSTOM_NAME);
        tag.remove(getDragonDataKey());
        tag.putBoolean(BinderComponentUtil.IS_BOUND, false);
    }

    protected void onDragonCaptured(T dragon, Player player, ItemStack binderStack) {
    }

    protected void onDragonReleased(T dragon, Player player, ItemStack binderStack) {
    }

    protected void prepareDragonForCapture(T dragon, Player player) {
    }

    protected void prepareDragonForRelease(T dragon, Player player) {
    }

    protected void prepareDragonForAirRelease(T dragon, Player player) {
    }

    protected String getNotOwnerMessageKey() {
        return "saintsdragons.message.not_dragon_owner";
    }

    protected String getReleaseNotOwnerMessageKey() {
        return "saintsdragons.message.cannot_release_others_dragon";
    }

    protected String getAlreadyOccupiedMessageKey() {
        return "saintsdragons.message.binder_already_occupied";
    }

    protected String getBoundReleaseTooltipKey() {
        return getTooltipReleaseKey();
    }

    protected boolean shouldShowReleaseTooltipWhenBound() {
        return true;
    }

    protected void appendExtraBoundTooltip(@NotNull java.util.function.Consumer<Component> tooltip, @Nullable String dragonName) {
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext tooltipContext, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(getTooltipDescriptionKey()));
        if (BinderComponentUtil.isBound(stack)) {
            String dragonName = BinderComponentUtil.getBoundDragonName(stack);
            if (dragonName != null && !dragonName.isEmpty()) {
                tooltip.accept(Component.translatable(getTooltipBoundKey(), dragonName));
            }
            appendExtraBoundTooltip(tooltip, dragonName);
            if (shouldShowReleaseTooltipWhenBound()) {
                tooltip.accept(Component.translatable(getBoundReleaseTooltipKey()));
            }
        } else {
            tooltip.accept(Component.translatable(getTooltipEmptyKey()));
            tooltip.accept(Component.translatable(getTooltipBindHintKey()));
        }
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return BinderComponentUtil.isBound(stack);
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity itemEntity) {
        BinderComponentUtil.handleDestroyedBoundBinder(itemEntity);
        super.onDestroyed(itemEntity);
    }

    protected abstract Class<T> getDragonClass();

    protected abstract T createDragon(ServerLevel level);

    protected abstract boolean canBind(T dragon);

    protected abstract String getDragonDataKey();

    protected String getCannotCaptureMessageKey() {
        return "saintsdragons.message.creature_cannot_be_captured";
    }

    protected String getCapturedMessageKey() {
        return "saintsdragons.message.creature_captured";
    }

    protected String getReleasedMessageKey() {
        return "saintsdragons.message.creature_released";
    }

    protected String getReleaseFailedMessageKey() {
        return "saintsdragons.message.creature_release_failed";
    }

    protected abstract String getTooltipDescriptionKey();

    protected String getTooltipBoundKey() {
        return "saintsdragons.tooltip.binder.bound";
    }

    protected String getTooltipEmptyKey() {
        return "saintsdragons.tooltip.binder.empty";
    }

    protected String getTooltipBindHintKey() {
        return "saintsdragons.tooltip.binder.right_click_to_bind";
    }

    protected String getTooltipReleaseKey() {
        return "saintsdragons.tooltip.binder.right_click_to_release";
    }
}
