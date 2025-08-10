package lunar.tinkerer;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.MathHelper;


public class ManathiefFaceModel extends Model implements EntityModelLayerRegistry.TexturedModelDataProvider {
    private final ModelPart face;
    private final ModelPart leaf1;
    private final ModelPart leaf2;
    private final ModelPart leaf3;

    public ManathiefFaceModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);
        this.face = root.getChild("face");
        this.leaf1 = root.getChild("leaf1");
        this.leaf2 = root.getChild("leaf2");
        this.leaf3 = root.getChild("leaf3");
    }

    public void setPageAngles(float pageTurnAmount, float leftFlipAmount, float rightFlipAmount, float pageTurnSpeed) {
    }

    public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData face =  modelPartData.addChild("face", ModelPartBuilder.create().uv(-8, 8).cuboid(-4.0F, -1.0F, -4.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.rotation(0.0F, 24.0F, 0.0F));
		ModelPartData leaf1 = modelPartData.addChild("leaf1", ModelPartBuilder.create().uv(-8, 0).cuboid(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, 0.0F, 1.0F, -0.0873F, 0.0F, 0.0873F));
		ModelPartData leaf2 = modelPartData.addChild("leaf2", ModelPartBuilder.create().uv(-8, 0).cuboid(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(-1.0F, 0.0F, 0.0F, 0.0F, -2.0944F, -0.1745F));
		ModelPartData leaf3 = modelPartData.addChild("leaf3", ModelPartBuilder.create().uv(-8, 0).cuboid(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, -1.0F, -0.2618F, 2.0944F, -0.1745F));
		return TexturedModelData.of(modelData, 16, 16);
	}


    @Override
    public TexturedModelData createModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData face =  modelPartData.addChild("face", ModelPartBuilder.create().uv(-8, 8).cuboid(-4.0F, -1.0F, -4.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.rotation(0.0F, 24.0F, 0.0F));
        ModelPartData leaf1 = modelPartData.addChild("leaf1", ModelPartBuilder.create().uv(-8, 0).cuboid(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, 0.0F, 1.0F, -0.0873F, 0.0F, 0.0873F));
        ModelPartData leaf2 = modelPartData.addChild("leaf2", ModelPartBuilder.create().uv(-8, 0).cuboid(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(-1.0F, 0.0F, 0.0F, 0.0F, -2.0944F, -0.1745F));
        ModelPartData leaf3 = modelPartData.addChild("leaf3", ModelPartBuilder.create().uv(-8, 0).cuboid(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, -1.0F, -0.2618F, 2.0944F, -0.1745F));
        return TexturedModelData.of(modelData, 16, 16);
    }
}
