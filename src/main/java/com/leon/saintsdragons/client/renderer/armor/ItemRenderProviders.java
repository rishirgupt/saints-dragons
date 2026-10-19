package com.leon.saintsdragons.client.renderer.armor;

import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.GeoItemRenderer;
import com.leon.saintsdragons.client.renderer.item.MossbackItemRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** GeckoLib render providers for the items that draw with a GeckoLib model. Client only. */
public final class ItemRenderProviders {
    private ItemRenderProviders() {
    }

    public static GeoRenderProvider dragonlordArmor() {
        return new GeoRenderProvider() {
            private DragonlordArmorRenderer<?> renderer;

            @Override
            public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
                if (renderer == null) {
                    renderer = new DragonlordArmorRenderer<>();
                }
                return renderer;
            }
        };
    }

    public static GeoRenderProvider bloodTempestArmor() {
        return new GeoRenderProvider() {
            private BloodTempestArmorRenderer<?> renderer;

            @Override
            public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
                if (renderer == null) {
                    renderer = new BloodTempestArmorRenderer<>();
                }
                return renderer;
            }
        };
    }

    public static GeoRenderProvider mossback() {
        return new GeoRenderProvider() {
            private MossbackItemRenderer renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new MossbackItemRenderer();
                }
                return renderer;
            }
        };
    }
}
