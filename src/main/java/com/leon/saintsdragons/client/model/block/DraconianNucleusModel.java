package com.leon.saintsdragons.client.model.block;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

public class DraconianNucleusModel extends net.minecraft.client.model.Model<DraconianNucleusModel.State> {
    /** What the model needs for one frame: which clip plays and how far into it. */
    public record State(boolean spawning, long animationTimeMillis) {
    }

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath("saintsdragons", "draconian_nucleus"), "main");

    private final net.minecraft.client.animation.KeyframeAnimation idleAnimation;
    private final net.minecraft.client.animation.KeyframeAnimation spawnAnimation;

    public DraconianNucleusModel(ModelPart bakedRoot) {
        super(bakedRoot, net.minecraft.client.renderer.rendertype.RenderTypes::entityTranslucent);
        this.idleAnimation = DraconianNucleusAnimations.IDLE.bake(bakedRoot);
        this.spawnAnimation = DraconianNucleusAnimations.SPAWN.bake(bakedRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition modelRoot = mesh.getRoot();
        PartDefinition root = modelRoot.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        PartDefinition group = root.addOrReplaceChild("group", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition outer = group.addOrReplaceChild("outerlayer",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        outer.addOrReplaceChild("innerlayer",
                CubeListBuilder.create().texOffs(0, 24)
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 48, 48);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        (state.spawning() ? this.spawnAnimation : this.idleAnimation).apply(state.animationTimeMillis(), 1.0F);
    }
}
