package com.leon.saintsdragons.fabric;

import com.leon.saintsdragons.client.init.CommonClientModEvents;
import com.leon.saintsdragons.fabric.client.FabricDragonRideKeybinds;
import com.leon.saintsdragons.fabric.client.FabricDragonUI;
import com.leon.saintsdragons.fabric.client.event.FabricClientEventHandler;
import com.leon.saintsdragons.fabric.client.particle.FabricParticleRegistry;
import com.leon.saintsdragons.fabric.entity.part.FabricPartEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;

public final class SaintsDragonsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        CommonClientModEvents.registerEntityRenderers(EntityRendererRegistry::register);
        CommonClientModEvents.registerMenuScreens();
        // Dragon hitbox parts are invisible; they only exist for targeting.
        EntityRendererRegistry.register(FabricPartEntities.DRAGON_PART, NoopRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                com.leon.saintsdragons.client.model.block.DraconianNucleusModel.LAYER_LOCATION,
                com.leon.saintsdragons.client.model.block.DraconianNucleusModel::createBodyLayer);
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                com.leon.saintsdragons.client.model.block.DraconicCrucibleEntity.LAYER_LOCATION,
                com.leon.saintsdragons.client.model.block.DraconicCrucibleEntity::createBodyLayer);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                com.leon.saintsdragons.common.registry.ModBlockEntities.DRACONIAN_NUCLEUS.get(),
                com.leon.saintsdragons.client.renderer.block.DraconianNucleusRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                com.leon.saintsdragons.common.registry.ModBlockEntities.DRACONIC_CRUCIBLE.get(),
                com.leon.saintsdragons.client.renderer.block.DraconicCrucibleRenderer::new);
        FabricParticleRegistry.register();
        FabricDragonRideKeybinds.init();
        FabricDragonUI.init();
        FabricClientEventHandler.init();
    }
}
