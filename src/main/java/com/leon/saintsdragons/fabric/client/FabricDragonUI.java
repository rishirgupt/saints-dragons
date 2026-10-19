package com.leon.saintsdragons.fabric.client;

import com.leon.saintsdragons.client.ui.DragonRideHealthBar;
import com.leon.saintsdragons.client.ui.SpeedLineOverlay;
import com.leon.saintsdragons.client.ui.DragonUIRegistry;
import com.leon.saintsdragons.client.ui.FireballChargeIndicator;
import com.leon.saintsdragons.client.ui.IgnivorusFireBreathMeterIndicator;
import com.leon.saintsdragons.client.ui.MeleeModeNotification;
import com.leon.saintsdragons.client.ui.RaevyxBeamMeterIndicator;
import com.leon.saintsdragons.client.ui.SwarmWaveBarOverlay;
import com.leon.saintsdragons.client.ui.VolitansBreathMeterIndicator;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.leon.saintsdragons.server.entity.dragons.ignivorus.Ignivorus;
import com.leon.saintsdragons.server.entity.dragons.raevyx.Raevyx;
import com.leon.saintsdragons.server.entity.dragons.volitans.Volitans;
import com.leon.saintsdragons.fabric.config.FabricClientConfigAccess;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Fabric-specific wiring for dragon UI overlay rendering
 */
public final class FabricDragonUI {
    private static final MeleeModeNotification meleeModeNotification = new MeleeModeNotification();
    private static final FireballChargeIndicator fireballChargeIndicator = new FireballChargeIndicator();
    private static final RaevyxBeamMeterIndicator raevyxBeamMeterIndicator = new RaevyxBeamMeterIndicator();
    private static final IgnivorusFireBreathMeterIndicator ignivorusFireBreathMeterIndicator = new IgnivorusFireBreathMeterIndicator();
    private static final VolitansBreathMeterIndicator volitansBreathMeterIndicator = new VolitansBreathMeterIndicator();
    private static final DragonRideHealthBar rideHealthBar = new DragonRideHealthBar();
    private static final SpeedLineOverlay diveSpeedLineOverlay = SpeedLineOverlay.INSTANCE;

    private static final KeyMapping TOGGLE_DRAGON_UI = new KeyMapping(
            "key.saintsdragons.toggle_dragon_ui",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F4,
            com.leon.saintsdragons.client.input.ModKeyCategory.MAIN
    );

    static {
        // Initialize the UI registry so other classes can access the melee mode notification
        DragonUIRegistry.init(meleeModeNotification);
    }

    private FabricDragonUI() {
    }

    public static void init() {
        registerVanillaHudHiding();
        KeyMappingHelper.registerKeyMapping(TOGGLE_DRAGON_UI);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            // Handle F4 toggle keybind
            if (client.gui.screen() == null) {
                while (TOGGLE_DRAGON_UI.consumeClick()) {
                    DragonUIRegistry.toggleUIVisibility();
                }
            } else {
                // Clear queued clicks so the key isn't processed when returning to game
                TOGGLE_DRAGON_UI.consumeClick();
            }

            // Tick all UI elements for smooth animations
            meleeModeNotification.tick();
            fireballChargeIndicator.tick();
            raevyxBeamMeterIndicator.tick();
            ignivorusFireBreathMeterIndicator.tick();
            volitansBreathMeterIndicator.tick();
        });

        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.addLast(com.leon.saintsdragons.common.SaintsDragonsCommon.rl("hud"), (graphics, deltaTracker) -> {
            float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                return;
            }

            int width = client.getWindow().getGuiScaledWidth();
            int height = client.getWindow().getGuiScaledHeight();

            SwarmWaveBarOverlay.render(graphics, width, tickDelta);

            com.leon.saintsdragons.client.camera.IgnivorusSkyfallScreenEffects.renderFlash(graphics, tickDelta);
            if (FabricClientConfigAccess.isDiveSpeedLinesEnabled()) {
                diveSpeedLineOverlay.extractRenderState(graphics, width, height, tickDelta);
            }

            // Get current dragon if riding
            DragonEntity currentDragon = null;
            if (client.player.getVehicle() instanceof DragonEntity dragon) {
                currentDragon = dragon;
                rideHealthBar.setDragon(dragon);
            }

            // Always render melee mode notification (independent of UI visibility toggle)
            meleeModeNotification.render(graphics, width, height);

            // Only render dragon UI elements if UI is visible
            if (!DragonUIRegistry.isUIVisible()) {
                return;
            }

            // Render dragon-specific UI when riding
            if (currentDragon instanceof Ignivorus ignivorus) {
                // Fireball charge indicator
                fireballChargeIndicator.setChargeLevel(ignivorus.getFireballChargeLevel());
                fireballChargeIndicator.extractRenderState(graphics, width, height, tickDelta);

                // Fire breath meter
                ignivorusFireBreathMeterIndicator.setBreathEnergy(ignivorus.getFireBreathEnergy());
                ignivorusFireBreathMeterIndicator.setBreathing(ignivorus.isBreathingFire());
                ignivorusFireBreathMeterIndicator.extractRenderState(graphics, width, height, tickDelta);
            } else if (currentDragon instanceof Raevyx raevyx) {
                // Beam meter for Raevyx
                raevyxBeamMeterIndicator.setBeamEnergy(raevyx.getBeamEnergy());
                raevyxBeamMeterIndicator.setBeaming(raevyx.isBeaming());
                raevyxBeamMeterIndicator.extractRenderState(graphics, width, height, tickDelta);
            } else if (currentDragon instanceof Volitans volitans) {
                volitansBreathMeterIndicator.setWaterEnergy(volitans.getWaterBreathEnergy());
                volitansBreathMeterIndicator.setPoisonEnergy(volitans.getPoisonBreathEnergy());
                volitansBreathMeterIndicator.setBreathMode(volitans.getBreathMode());
                volitansBreathMeterIndicator.setBreathing(volitans.isBreathing());
                volitansBreathMeterIndicator.extractRenderState(graphics, width, height, tickDelta);
            }

            // Render dragon ride health bar when riding any dragon
            if (currentDragon != null) {
                rideHealthBar.extractRenderState(graphics, width, height, tickDelta);
            }
        });
    }

    /**
     * Hides the vanilla status bars while riding a dragon with the dragon UI visible (F4 toggles it).
     * Replaces the 1.20.1 Gui mixin with Fabric's HUD element API.
     */
    private static void registerVanillaHudHiding() {
        java.util.function.BooleanSupplier ridingWithDragonUi = () -> {
            Minecraft client = Minecraft.getInstance();
            return client.player != null
                    && client.player.getVehicle() instanceof com.leon.saintsdragons.server.entity.base.DragonEntity
                    && DragonUIRegistry.isUIVisible();
        };
        java.util.function.BooleanSupplier hideInfoBar = () -> {
            Minecraft client = Minecraft.getInstance();
            return ridingWithDragonUi.getAsBoolean()
                    && !(client.player.getVehicle() instanceof net.minecraft.world.entity.PlayerRideableJumping);
        };
        hideWhen(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.HEALTH_BAR, ridingWithDragonUi);
        hideWhen(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.ARMOR_BAR, ridingWithDragonUi);
        hideWhen(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.FOOD_BAR, ridingWithDragonUi);
        hideWhen(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.AIR_BAR, ridingWithDragonUi);
        hideWhen(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.MOUNT_HEALTH, ridingWithDragonUi);
        hideWhen(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.INFO_BAR, hideInfoBar);
        hideWhen(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.EXPERIENCE_LEVEL, hideInfoBar);
    }

    private static void hideWhen(net.minecraft.resources.Identifier element, java.util.function.BooleanSupplier hidden) {
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.replaceElement(element, original ->
                (graphics, deltaTracker) -> {
                    if (!hidden.getAsBoolean()) {
                        original.extractRenderState(graphics, deltaTracker);
                    }
                });
    }
}
