package com.leon.saintsdragons.client.renderer.item;

import com.geckolib.renderer.GeoItemRenderer;
import com.leon.saintsdragons.client.model.item.MossbackItemModel;
import com.leon.saintsdragons.common.item.MossbackItem;

/**
 * Draws a held or dropped Mossback with its entity model. The inventory icon is a flat sprite, selected in
 * the item definition (assets/saintsdragons/items/mossback.json) rather than here.
 */
public class MossbackItemRenderer extends GeoItemRenderer<MossbackItem> {
    public MossbackItemRenderer() {
        super(new MossbackItemModel());
    }
}
