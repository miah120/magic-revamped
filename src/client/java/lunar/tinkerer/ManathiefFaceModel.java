package lunar.tinkerer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;


public class ManathiefFaceModel extends Model<ManathiefFaceModel.ManathiefFaceModelState> {

    public ManathiefFaceModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        root.getChild("face");
        root.getChild("leaf1");
        root.getChild("leaf2");
        root.getChild("leaf3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("face", CubeListBuilder.create(). texOffs(-8, 8).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 0.0F, 8.0F), PartPose.rotation(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("leaf1", CubeListBuilder.create().texOffs(-8, 0).addBox(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F), PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, -0.0873F, 0.0F, 0.0873F));
        root.addOrReplaceChild("leaf2", CubeListBuilder.create().texOffs(-8, 0).addBox(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, -2.0944F, -0.1745F));
        root.addOrReplaceChild("leaf3", CubeListBuilder.create().texOffs(-8, 0).addBox(-8.0F, 0.0F, -8.0F, 8.0F, 0.0F, 8.0F), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, -0.2618F, 2.0944F, -0.1745F));
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Environment(EnvType.CLIENT)
    public record ManathiefFaceModelState() {
    }
}
