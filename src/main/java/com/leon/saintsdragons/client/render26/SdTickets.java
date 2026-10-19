package com.leon.saintsdragons.client.render26;

import com.geckolib.constant.dataticket.DataTicket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/** Render-state data carried from extraction to the render pass for the mod's GeckoLib renderers. */
public final class SdTickets {
    /** The entity being rendered. Client entities are only touched on the render thread, as in 1.20.1. */
    public static final DataTicket<Entity> ENTITY = DataTicket.create("saintsdragons_entity", Entity.class);
    public static final DataTicket<Identifier> MODEL = DataTicket.create("saintsdragons_model", Identifier.class);
    public static final DataTicket<Identifier> TEXTURE = DataTicket.create("saintsdragons_texture", Identifier.class);
    public static final DataTicket<Float> MODEL_SCALE = DataTicket.create("saintsdragons_model_scale", Float.class);
    public static final DataTicket<EntityModelData> MODEL_DATA =
            DataTicket.create("saintsdragons_model_data", EntityModelData.class);

    private SdTickets() {
    }
}
