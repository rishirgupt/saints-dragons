package com.leon.saintsdragons.client.model.block;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;

public class DraconicCrucibleEntity extends net.minecraft.client.model.Model<DraconicCrucibleEntity.State> {
	/** What the model needs for one frame: which clip plays and how far into it. */
	public record State(boolean open, long animationTimeMillis) {
	}

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("saintsdragons", "draconic_crucible"), "main");
	private final net.minecraft.client.animation.KeyframeAnimation openAnimation;
	private final net.minecraft.client.animation.KeyframeAnimation closeAnimation;

	public DraconicCrucibleEntity(ModelPart root) {
		super(root, net.minecraft.client.renderer.rendertype.RenderTypes::entityCutout);
		this.openAnimation = DraconicCrucibleAnimations.OPEN.bake(root);
		this.closeAnimation = DraconicCrucibleAnimations.CLOSE.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(-1.0F, 20.0F, 0.0F));

		PartDefinition neck = root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -12.0F, -7.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition upperjaw = root.addOrReplaceChild("upperjaw", CubeListBuilder.create().texOffs(0, 32).addBox(0.0F, -14.0F, -10.0F, 11.0F, 14.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(39, 94).addBox(-2.0F, -14.0F, -10.0F, 2.0F, 6.0F, 20.0F, new CubeDeformation(0.01F))
				.texOffs(64, 0).addBox(0.0F, -25.0F, -8.0F, 11.0F, 11.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(0, 101).addBox(-4.0F, -25.0F, -8.0F, 4.0F, 11.0F, 16.0F, new CubeDeformation(0.01F)), PartPose.offset(1.0F, -2.0F, 0.0F));

		PartDefinition cube_r1 = upperjaw.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(54, 66).addBox(-1.0F, -5.75F, -10.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.01F))
				.texOffs(54, 66).addBox(-1.0F, -5.75F, -25.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(11.0F, 0.6F, 15.0F, 0.0F, 0.0F, -0.3927F));

		PartDefinition lowerjaw = root.addOrReplaceChild("lowerjaw", CubeListBuilder.create().texOffs(0, 101).mirror().addBox(0.0F, -23.0F, -8.0F, 4.0F, 11.0F, 16.0F, new CubeDeformation(-0.01F)).mirror(false)
				.texOffs(62, 32).addBox(-11.0F, -14.0F, -10.0F, 11.0F, 14.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(0, 66).addBox(-11.0F, -25.0F, -8.0F, 11.0F, 11.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -2.0F, 0.0F));

		PartDefinition cube_r2 = lowerjaw.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(54, 66).mirror().addBox(-4.0F, -5.75F, -10.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.01F)).mirror(false)
				.texOffs(54, 66).mirror().addBox(-4.0F, -5.75F, -25.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.01F)).mirror(false), PartPose.offsetAndRotation(-11.0F, 0.6F, 15.0F, 0.0F, 0.0F, 0.3927F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(State state) {
		super.setupAnim(state);
		(state.open() ? this.openAnimation : this.closeAnimation).apply(state.animationTimeMillis(), 1.0F);
	}
}
