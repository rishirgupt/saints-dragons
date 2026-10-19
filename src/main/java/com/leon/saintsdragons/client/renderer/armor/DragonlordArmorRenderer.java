package com.leon.saintsdragons.client.renderer.armor;

import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.leon.saintsdragons.client.model.armor.DragonlordArmorModel;
import com.leon.saintsdragons.client.render26.EntityModelData;
import com.leon.saintsdragons.client.render26.PoseState;
import com.leon.saintsdragons.client.render26.SdTickets;
import com.leon.saintsdragons.common.item.DragonlordArmorItem;
import com.leon.saintsdragons.common.item.DragonlordArmorSetBonus;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class DragonlordArmorRenderer<R extends HumanoidRenderState & GeoRenderState>
        extends GeoArmorRenderer<DragonlordArmorItem, R> {
    private final DragonlordArmorModel armorModel;

    public DragonlordArmorRenderer() {
        this(new DragonlordArmorModel());
    }

    private DragonlordArmorRenderer(DragonlordArmorModel model) {
        super(model);
        this.armorModel = model;
    }

    @Override
    public void captureDefaultRenderState(DragonlordArmorItem animatable, RenderData renderData, R renderState,
                                          float partialTick) {
        super.captureDefaultRenderState(animatable, renderData, renderState, partialTick);
        LivingEntity wearer = renderData.entity();
        renderState.addGeckolibData(SdTickets.ENTITY, wearer);
        // The wings only open while the wearer glides in the full set.
        renderState.addGeckolibData(DragonlordArmorItem.GLIDING,
                wearer.isFallFlying() && DragonlordArmorSetBonus.isWearingFullSet(wearer));
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        R state = renderPassInfo.renderState();
        Entity wearer = state.getGeckolibData(SdTickets.ENTITY);
        if (wearer != null) {
            float partialTick = state.getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 1.0F);
            DragonlordArmorItem item = (DragonlordArmorItem) com.leon.saintsdragons.common.registry.ModArmors
                    .DRAGONLORD_CHESTPLATE.get();
            armorModel.pose(item, wearer.getId(),
                    new PoseState<>(item, partialTick, new EntityModelData(false, false, 0.0F, 0.0F), wearer),
                    renderPassInfo.model(), snapshots);
        }
        // Matching the armour bones to the wearer's limbs comes last so it wins over the wing pose.
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
    }
}
