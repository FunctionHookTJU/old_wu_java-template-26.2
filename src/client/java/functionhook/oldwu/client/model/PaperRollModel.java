package functionhook.oldwu.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import functionhook.oldwu.Old_Wu_java;
import functionhook.oldwu.client.render.PaperRollRenderState;

/**
 * 纸卷模型（Blockbench 导出后适配 26.2 渲染 API，64×64）。
 * 几何已居中于原点，长轴沿 Z 轴（渲染器只需按 yaw/pitch 旋转即可对齐飞行方向）。
 */
public class PaperRollModel extends EntityModel<PaperRollRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Old_Wu_java.id("paper_roll"), "main");

	private final ModelPart bone;

	public PaperRollModel(ModelPart root) {
		super(root);
		this.bone = root.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// 原 Blockbench 骨轴为 (0,16,15)，几何中心位于 (0,18,3.5)；改为 (0,-2,11.5) 使卷轴居中于原点
		PartDefinition bone = partdefinition.addOrReplaceChild(
			"bone",
			CubeListBuilder.create()
				.texOffs(21, 0).addBox(-5.0F, -23.0F, 0.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-6.0F, -23.0F, -4.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(7, 0).addBox(-6.0F, -23.0F, -2.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(14, 0).addBox(-5.0F, -23.0F, -6.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(28, 0).addBox(4.0F, -23.0F, -6.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(28, 26).addBox(5.0F, -23.0F, -4.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(35, 0).addBox(5.0F, -23.0F, -2.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(35, 26).addBox(4.0F, -23.0F, 0.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -2.0F, 11.5F, 1.5708F, 0.0F, 0.0F)
		);

		bone.addOrReplaceChild(
			"cube_r1",
			CubeListBuilder.create().texOffs(49, 26).addBox(0.0F, -23.0F, 0.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-4.0F, 0.0F, 3.0F, 0.0F, 1.5708F, 0.0F)
		);

		bone.addOrReplaceChild(
			"cube_r2",
			CubeListBuilder.create().texOffs(49, 0).addBox(0.0F, -23.0F, 0.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-2.0F, 0.0F, 4.0F, 0.0F, 1.5708F, 0.0F)
		);

		bone.addOrReplaceChild(
			"cube_r3",
			CubeListBuilder.create().texOffs(42, 26).addBox(0.0F, -23.0F, 0.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, 1.5708F, 0.0F)
		);

		bone.addOrReplaceChild(
			"cube_r4",
			CubeListBuilder.create().texOffs(42, 0).addBox(0.0F, -23.0F, 0.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(2.0F, 0.0F, 3.0F, 0.0F, 1.5708F, 0.0F)
		);

		bone.addOrReplaceChild(
			"cube_r5",
			CubeListBuilder.create().texOffs(21, 26).addBox(0.0F, -23.0F, 0.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(2.0F, 0.0F, -6.0F, 0.0F, 1.5708F, 0.0F)
		);

		bone.addOrReplaceChild(
			"cube_r6",
			CubeListBuilder.create().texOffs(14, 26).addBox(0.0F, -23.0F, 0.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 0.0F, -7.0F, 0.0F, 1.5708F, 0.0F)
		);

		bone.addOrReplaceChild(
			"cube_r7",
			CubeListBuilder.create().texOffs(7, 26).addBox(0.0F, -23.0F, 0.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-2.0F, 0.0F, -7.0F, 0.0F, 1.5708F, 0.0F)
		);

		bone.addOrReplaceChild(
			"cube_r8",
			CubeListBuilder.create().texOffs(0, 26).addBox(0.0F, -23.0F, 0.0F, 1.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-4.0F, 0.0F, -6.0F, 0.0F, 1.5708F, 0.0F)
		);

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(PaperRollRenderState state) {
		super.setupAnim(state);
	}
}
