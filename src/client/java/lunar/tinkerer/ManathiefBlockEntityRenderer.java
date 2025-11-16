package lunar.tinkerer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.EnchantingTableBlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class ManathiefBlockEntityRenderer
        implements BlockEntityRenderer<ManathiefBlockEntity, EnchantingTableBlockEntityRenderState> {
    //TODO: Make this work
    private static final Identifier TEXTURE = MagicRevamped.identifier("textures/entity/manathief_face.png");
    private static final RenderLayer MANATHIEF_FACE = RenderLayer.getEntityCutout(TEXTURE);
    private final ManathiefFaceModel face;

    public ManathiefBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.face = new ManathiefFaceModel(ctx.getLayerModelPart(ModModelLayers.MANATHIEF_FACE_MODEL_LAYER));
    }

    public void render(ManathiefBlockEntity manathiefBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, Vec3d vec3d) {
        matrixStack.push();
        matrixStack.translate(0.5f, 0.75f, 0.5f);
        float g = (float) manathiefBlockEntity.ticks + f;
        matrixStack.translate(0.0f, 0.1f + MathHelper.sin(g * 0.1f) * 0.01f, 0.0f);
        float h2 = ManathiefBlockEntity.normalizeRotation(manathiefBlockEntity.bookRotation - manathiefBlockEntity.lastBookRotation);
        float k = manathiefBlockEntity.lastBookRotation + h2 * f;
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(-k));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-110));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(60.0f));
        //this.face.setPageAngles(g, 1, 0, 1);
        this.face.render(matrixStack, vertexConsumerProvider.getBuffer(MANATHIEF_FACE), i, OverlayTexture.DEFAULT_UV);
        matrixStack.pop();
    }

    @Override
    public EnchantingTableBlockEntityRenderState createRenderState() {
        return new EnchantingTableBlockEntityRenderState();
    }

    @Override
    public void render(EnchantingTableBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {

    }
}

