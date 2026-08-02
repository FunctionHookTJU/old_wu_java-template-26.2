// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class angry_baby<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "angry_baby"), "main");
	private final ModelPart body;
	private final ModelPart belly;
	private final ModelPart head;
	private final ModelPart tail1;
	private final ModelPart backLegL;
	private final ModelPart backLegR;
	private final ModelPart frontLegL;
	private final ModelPart frontLegR;

	public angry_baby(ModelPart root) {
		this.body = root.getChild("body");
		this.belly = this.body.getChild("belly");
		this.head = this.body.getChild("head");
		this.tail1 = this.body.getChild("tail1");
		this.backLegL = this.body.getChild("backLegL");
		this.backLegR = this.body.getChild("backLegR");
		this.frontLegL = this.body.getChild("frontLegL");
		this.frontLegR = this.body.getChild("frontLegR");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 0.0F));

		PartDefinition belly = body.addOrReplaceChild("belly", CubeListBuilder.create().texOffs(-3, 8).addBox(-4.0F, 4.5F, -6.5F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, 0.5F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.0F, -2.875F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(18, 0).addBox(-2.0F, -3.0F, -0.875F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(24, 0).addBox(1.0F, -3.0F, -0.875F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(18, 3).addBox(-1.5F, 0.0F, -3.875F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.125F));

		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -4.893F, 3.9151F, -0.5672F, 0.0F, 0.0F));

		PartDefinition tail1_r1 = tail1.addOrReplaceChild("tail1_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -2.0F, 4.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.893F, 0.0849F, 0.5672F, 0.0F, 0.0F));

		PartDefinition backLegL = body.addOrReplaceChild("backLegL", CubeListBuilder.create(), PartPose.offset(1.0F, -2.0F, 2.5F));

		PartDefinition backLegL_r1 = backLegL.addOrReplaceChild("backLegL_r1", CubeListBuilder.create().texOffs(17, 21).addBox(0.5F, -1.0F, 1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 3.0F, -2.5F, 0.0F, -1.5708F, 0.0F));

		PartDefinition backLegR = body.addOrReplaceChild("backLegR", CubeListBuilder.create(), PartPose.offset(-1.0F, -2.0F, 2.5F));

		PartDefinition backLegR_r1 = backLegR.addOrReplaceChild("backLegR_r1", CubeListBuilder.create().texOffs(11, 21).addBox(-1.5F, -1.0F, 1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 3.0F, -2.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition frontLegL = body.addOrReplaceChild("frontLegL", CubeListBuilder.create(), PartPose.offset(-6.0F, -2.0F, -1.5F));

		PartDefinition frontLegL_r1 = frontLegL.addOrReplaceChild("frontLegL_r1", CubeListBuilder.create().texOffs(17, 17).addBox(0.5F, -1.0F, -2.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 3.0F, -2.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition frontLegR = body.addOrReplaceChild("frontLegR", CubeListBuilder.create(), PartPose.offset(-1.0F, -2.0F, -1.5F));

		PartDefinition frontLegR_r1 = frontLegR.addOrReplaceChild("frontLegR_r1", CubeListBuilder.create().texOffs(11, 17).addBox(-1.5F, -1.0F, -2.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 3.0F, -2.5F, 0.0F, -1.5708F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}