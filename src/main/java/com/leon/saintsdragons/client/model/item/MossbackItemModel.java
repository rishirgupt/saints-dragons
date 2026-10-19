package com.leon.saintsdragons.client.model.item;

import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.leon.saintsdragons.client.render26.PosedGeoModel;
import com.leon.saintsdragons.client.render26.SdTickets;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.item.MossbackItem;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** The held Mossback: the adult or baby entity model, chosen from the item stack being drawn. */
public class MossbackItemModel extends PosedGeoModel<MossbackItem> {
    private static final Identifier ADULT = SaintsDragonsCommon.rl("entity/mossback/mossback");
    private static final Identifier BABY = SaintsDragonsCommon.rl("entity/mossback/baby_mossback");
    private static final Identifier ADULT_TEXTURE = SaintsDragonsCommon.rl("textures/entity/mossback/mossback.png");
    private static final Identifier BABY_TEXTURE = SaintsDragonsCommon.rl("textures/entity/mossback/baby_mossback.png");

    @Override
    public void addAdditionalStateData(MossbackItem animatable, @Nullable Object relatedObject,
                                       GeoRenderState renderState) {
        boolean baby = relatedObject instanceof GeoItemRenderer.RenderData data && MossbackItem.isBaby(data.itemStack());
        renderState.addGeckolibData(SdTickets.MODEL, baby ? BABY : ADULT);
        renderState.addGeckolibData(SdTickets.TEXTURE, baby ? BABY_TEXTURE : ADULT_TEXTURE);
        // The item's animation controller picks the baby or adult idle clip from this.
        renderState.addGeckolibData(MossbackItem.BABY, baby);
    }

    @Override
    public Identifier getModelResource(MossbackItem animatable) {
        return ADULT;
    }

    @Override
    public Identifier getTextureResource(MossbackItem animatable) {
        return ADULT_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(MossbackItem animatable) {
        return ADULT;
    }

    @Override
    public Identifier[] getAnimationResourceFallbacks(MossbackItem animatable) {
        // The baby clips live in their own file.
        return new Identifier[] {BABY};
    }
}
