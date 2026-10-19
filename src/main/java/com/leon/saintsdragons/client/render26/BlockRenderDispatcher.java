package com.leon.saintsdragons.client.render26;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Draws a single block state from effect code that was written against the 1.20.1 block render dispatcher.
 * The block is queued on the {@link RecordingBufferSource} and submitted with the rest of its geometry.
 */
public final class BlockRenderDispatcher {
    private final BlockModelResolver resolver;

    public BlockRenderDispatcher(BlockModelResolver resolver) {
        this.resolver = resolver;
    }

    /** The dispatcher backed by the game's block model resolver. */
    public static BlockRenderDispatcher get() {
        return new BlockRenderDispatcher(new BlockModelResolver(Minecraft.getInstance().getModelManager()));
    }

    public void renderSingleBlock(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, int packedOverlay) {
        if (!(bufferSource instanceof RecordingBufferSource recorder)) {
            return;
        }
        BlockModelRenderState renderState = new BlockModelRenderState();
        resolver.update(renderState, state, BlockDisplayContext.create());
        if (renderState.isEmpty()) {
            return;
        }
        PoseStack.Pose pose = poseStack.last().copy();
        recorder.defer(renderTasks -> {
            PoseStack replay = new PoseStack();
            replay.last().set(pose);
            renderState.submit(replay, renderTasks, packedLight, packedOverlay, 0);
        });
    }
}
