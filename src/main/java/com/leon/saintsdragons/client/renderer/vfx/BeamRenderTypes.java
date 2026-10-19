package com.leon.saintsdragons.client.renderer.vfx;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Unlit alpha-blended render type used by beams, flashes and other effect geometry. */
public final class BeamRenderTypes {
    private BeamRenderTypes() {
    }

    public static RenderType translucent(Identifier texture) {
        // Translucent, unculled, full-bright and without depth writes: the vanilla emissive entity type.
        return RenderTypes.entityTranslucentEmissive(texture);
    }
}
