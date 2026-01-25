package lunar.tinkerer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayers;


public class ManathiefFaceModel extends Model<ManathiefFaceModel.ManathiefFaceModelState> {

    public ManathiefFaceModel(ModelPart root) {
        super(root, RenderLayers::entitySolid);
        root.getChild("face");
        root.getChild("leaf1");
        root.getChild("leaf2");
        root.getChild("leaf3");
    }

    public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("face", ModelPartBuilder.create().uv(-8, 8).cuboid(-4.0F, -1.0F, -4.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.rotation(0.0F, 24.0F, 0.0F));
        modelPartData.addChild("leaf1", ModelPartBuilder.create().uv(-8, 0).cuboid(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, 0.0F, 1.0F, -0.0873F, 0.0F, 0.0873F));
        modelPartData.addChild("leaf2", ModelPartBuilder.create().uv(-8, 0).cuboid(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(-1.0F, 0.0F, 0.0F, 0.0F, -2.0944F, -0.1745F));
        modelPartData.addChild("leaf3", ModelPartBuilder.create().uv(-8, 0).cuboid(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, -1.0F, -0.2618F, 2.0944F, -0.1745F));
        return TexturedModelData.of(modelData, 16, 16);
	}

    @Environment(EnvType.CLIENT)
    public record ManathiefFaceModelState() {
    }
}
