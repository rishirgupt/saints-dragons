package com.leon.saintsdragons.client.renderer.layer.npc;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.leon.saintsdragons.client.render26.ModGeoEntityRenderer;
import com.leon.saintsdragons.client.render26.SdLivingState;
import com.leon.saintsdragons.server.entity.npc.IvyTheDragonMerchant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Draws the item Ivy is holding (her sword, or the item she is handing back) at her right-hand locator bone. */
public class IvyHeldItemLayer extends BlockAndItemGeoLayer<IvyTheDragonMerchant, Void, SdLivingState> {
    private static final String RIGHT_HAND_LOCATOR = "rightArmItemLocator";

    public IvyHeldItemLayer(EntityRendererProvider.Context context,
                            ModGeoEntityRenderer<IvyTheDragonMerchant, SdLivingState> renderer) {
        super(context, renderer);
    }

    private static ItemStack heldStack(IvyTheDragonMerchant animatable) {
        ItemStack stack = animatable.getRecoveryItemForRender();
        if (!stack.isEmpty()) {
            return stack;
        }
        return animatable.getSwordForRender();
    }

    @Override
    protected List<RenderData> getRelevantBones(IvyTheDragonMerchant animatable, @Nullable Void relatedObject,
                                                SdLivingState renderState, float partialTick) {
        ItemStack stack = heldStack(animatable);
        if (stack.isEmpty()) {
            return List.of();
        }
        return List.of(RenderData.item(RIGHT_HAND_LOCATOR, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                RenderUtil.createRenderStateForItem(stack, this.itemModelResolver,
                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, animatable)));
    }

    @Override
    public void addRenderData(IvyTheDragonMerchant animatable, @Nullable Void relatedObject, SdLivingState renderState,
                              float partialTick) {
        renderState.addGeckolibData(CONTENTS, getRelevantBones(animatable, relatedObject, renderState, partialTick));
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState stackState,
                                         ItemDisplayContext displayContext, SdLivingState renderState,
                                         SubmitNodeCollector renderTasks, int packedLight) {
        poseStack.translate(0.0D, -0.0625D, -0.1D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        super.submitItemStackRender(poseStack, bone, stackState, displayContext, renderState, renderTasks, packedLight);
    }
}
