package com.leon.saintsdragons.client.render26;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Stand-in for the vanilla buffer source that 26.2 removed. Effect code asks it for a vertex consumer per
 * render type exactly as before; see {@link RecordingBufferSource} for how the vertices reach the screen.
 */
public interface MultiBufferSource {
    VertexConsumer getBuffer(RenderType renderType);
}
