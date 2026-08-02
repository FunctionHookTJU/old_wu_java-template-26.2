// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class flat<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "flat"), "main");
	private final ModelPart body;
	private final ModelPart belly;
	private final ModelPart head;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart backLegL;
	private final ModelPart backLegR;
	private final ModelPart frontLegL;
	private final ModelPart frontLegR;

	public flat(ModelPart root) {
		this.body = root.getChild("body");
		this.belly = this.body.getChild("belly");
		this.head = this.body.getChild("head");
		this.tail1 = this.body.getChild("tail1");
		this.tail2 = this.tail1.getChild("tail2");
		this.backLegL = this.body.getChild("backLegL");
		this.backLegR = this.body.getChild("backLegR");
		this.frontLegL = this.body.getChild("frontLegL");
		this.frontLegR = this.body.getChild("frontLegR");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 21.0F, 1.0F));

		PartDefinition belly = body.addOrReplaceChild("belly", CubeListBuilder.create().texOffs(20, 6).addBox(-5.0F, -8.0F, -3.0F, 10.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -1.0F, -3.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 24).addBox(-1.5F, 0.9844F, -4.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 10).addBox(-2.0F, -2.0F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(6, 10).addBox(1.0F, -2.0F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, -10.0F));

		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition tail1_r1 = tail1.addOrReplaceChild("tail1_r1", CubeListBuilder.create().texOffs(0, 15).addBox(-0.5F, -6.0F, -1.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(4, 15).addBox(-0.5F, 3.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition backLegL = body.addOrReplaceChild("backLegL", CubeListBuilder.create(), PartPose.offset(1.1F, 1.0F, 5.0F));

		PartDefinition backLegL_r1 = backLegL.addOrReplaceChild("backLegL_r1", CubeListBuilder.create().texOffs(9, 13).addBox(1.1F, 8.0F, 6.0F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.1F, 4.0F, -7.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition backLegR = body.addOrReplaceChild("backLegR", CubeListBuilder.create(), PartPose.offset(-1.1F, 1.0F, 5.0F));

		PartDefinition backLegR_r1 = backLegR.addOrReplaceChild("backLegR_r1", CubeListBuilder.create().texOffs(9, 13).addBox(-1.1F, 8.0F, 6.0F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1F, 3.0F, -7.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition frontLegL = body.addOrReplaceChild("frontLegL", CubeListBuilder.create(), PartPose.offset(1.2F, -3.0F, -5.0F));

		PartDefinition frontLegL_r1 = frontLegL.addOrReplaceChild("frontLegL_r1", CubeListBuilder.create().texOffs(41, 0).addBox(1.2F, 7.8F, -5.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2F, 8.0F, 4.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition frontLegR = body.addOrReplaceChild("frontLegR", CubeListBuilder.create(), PartPose.offset(-1.2F, -3.0F, -5.0F));

		PartDefinition frontLegR_r1 = frontLegR.addOrReplaceChild("frontLegR_r1", CubeListBuilder.create().texOffs(41, 0).addBox(-1.2F, 7.8F, -5.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, 7.0F, 4.0F, 0.0F, 0.0F, 1.5708F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}