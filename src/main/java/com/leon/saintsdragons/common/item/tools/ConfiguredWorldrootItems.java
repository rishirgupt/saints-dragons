package com.leon.saintsdragons.common.item.tools;

import com.leon.saintsdragons.common.config.ToolsArmorConfig;
import com.leon.saintsdragons.common.item.ConfiguredItemAttributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;

/**
 * Worldroot tools. Attack damage and speed come from the config; since 1.21 they are baked into the item's
 * attribute component when the item is constructed, so config changes apply after a restart.
 */
public final class ConfiguredWorldrootItems {
    private static final WorldrootTier TIER = WorldrootTier.INSTANCE;

    private static float damage(double configured) {
        return ConfiguredItemAttributes.toolDamageModifier(configured, TIER.getAttackDamageBonus());
    }

    private static float speed(double configured) {
        return ConfiguredItemAttributes.toolSpeedModifier(configured);
    }

    public static final class Sword extends Item {
        public Sword(Item.Properties properties) {
            super(properties.sword(TIER.material(),
                    damage(ToolsArmorConfig.WORLDROOT_SWORD_DAMAGE.get()),
                    speed(ToolsArmorConfig.WORLDROOT_SWORD_SPEED.get())));
        }
    }

    public static final class Pickaxe extends Item {
        public Pickaxe(Item.Properties properties) {
            super(properties.pickaxe(TIER.material(),
                    damage(ToolsArmorConfig.WORLDROOT_PICKAXE_DAMAGE.get()),
                    speed(ToolsArmorConfig.WORLDROOT_PICKAXE_SPEED.get())));
        }
    }

    public static final class Axe extends AxeItem {
        public Axe(Item.Properties properties) {
            super(TIER.material(),
                    damage(ToolsArmorConfig.WORLDROOT_AXE_DAMAGE.get()),
                    speed(ToolsArmorConfig.WORLDROOT_AXE_SPEED.get()), properties);
        }
    }

    public static final class Shovel extends ShovelItem {
        public Shovel(Item.Properties properties) {
            super(TIER.material(),
                    damage(ToolsArmorConfig.WORLDROOT_SHOVEL_DAMAGE.get()),
                    speed(ToolsArmorConfig.WORLDROOT_SHOVEL_SPEED.get()), properties);
        }
    }

    public static final class Hoe extends HoeItem {
        public Hoe(Item.Properties properties) {
            super(TIER.material(),
                    damage(ToolsArmorConfig.WORLDROOT_HOE_DAMAGE.get()),
                    speed(ToolsArmorConfig.WORLDROOT_HOE_SPEED.get()), properties);
        }
    }

    private ConfiguredWorldrootItems() {
    }
}
