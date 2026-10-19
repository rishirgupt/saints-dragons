package com.leon.saintsdragons.fabric.client;

import com.leon.saintsdragons.client.input.BloodTempestDodgeInput;
import com.leon.saintsdragons.client.input.DragonRideInputHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

/**
 * Fabric glue that registers the shared dragon riding keybinds and ticking behaviour.
 */
public final class FabricDragonRideKeybinds {
    private FabricDragonRideKeybinds() {
    }

    public static void init() {
        DragonRideInputHandler.registerKeys(KeyMappingHelper::registerKeyMapping, true);

        // Register tick event for handling input
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            BloodTempestDodgeInput.clientTick();
            DragonRideInputHandler.clientTick();
        });
    }
}

