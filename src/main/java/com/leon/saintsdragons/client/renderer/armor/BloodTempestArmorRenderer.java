package com.leon.saintsdragons.client.renderer.armor;

import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.leon.saintsdragons.client.model.armor.BloodTempestArmorModel;
import com.leon.saintsdragons.common.item.BloodTempestArmorItem;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class BloodTempestArmorRenderer<R extends HumanoidRenderState & GeoRenderState>
        extends GeoArmorRenderer<BloodTempestArmorItem, R> {
    public BloodTempestArmorRenderer() {
        super(new BloodTempestArmorModel());
    }

    @Override
    public String getBoneNameForSegment(R renderState, ArmorSegment segment) {
        // This model names its head bone in lower case.
        return segment == ArmorSegment.HEAD ? "armorhead" : super.getBoneNameForSegment(renderState, segment);
    }
}
