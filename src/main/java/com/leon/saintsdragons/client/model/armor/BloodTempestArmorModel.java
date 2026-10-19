package com.leon.saintsdragons.client.model.armor;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.item.BloodTempestArmorItem;
import net.minecraft.resources.Identifier;
import com.leon.saintsdragons.client.render26.PosedGeoModel;

public class BloodTempestArmorModel extends PosedGeoModel<BloodTempestArmorItem> {
    private static final Identifier MODEL =
            SaintsDragonsCommon.rl("armor/blood_tempest_armor");
    private static final Identifier TEXTURE =
            SaintsDragonsCommon.rl("textures/armor/blood_tempest_armor.png");
    private static final Identifier ANIMATION =
            SaintsDragonsCommon.rl("armor/blood_tempest_armor");

    @Override
    public Identifier getModelResource(BloodTempestArmorItem animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(BloodTempestArmorItem animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(BloodTempestArmorItem animatable) {
        return ANIMATION;
    }
}
