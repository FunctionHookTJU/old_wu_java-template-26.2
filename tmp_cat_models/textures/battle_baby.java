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

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, 23.0F, 0.0F, 0.0F, 0.0F, -1.4399F));

		PartDefinition belly = body.addOrReplaceChild("belly", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -1.5F, -3.5F, 4.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, 0.5F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.0F, -2.875F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(18, 0).addBox(-2.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(24, 0).addBox(1.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(18, 3).addBox(-1.5F, -1.0F, -3.875F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -4.125F));

		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -0.107F, 0.0849F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.893F, 3.9151F, -0.5672F, 0.0F, 0.0F));

		PartDefinition backLegL = body.addOrReplaceChild("backLegL", CubeListBuilder.create().texOffs(18, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -2.0F, 2.5F));

		PartDefinition backLegR = body.addOrReplaceChild("backLegR", CubeListBuilder.create().texOffs(12, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -2.0F, 2.5F));

		PartDefinition frontLegL = body.addOrReplaceChild("frontLegL", CubeListBuilder.create().texOffs(18, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -2.0F, -1.5F));

		PartDefinition frontLegR = body.addOrReplaceChild("frontLegR", CubeListBuilder.create().texOffs(12, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -2.0F, -1.5F));

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