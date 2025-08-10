package lunar.tinkerer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class ManathiefBlockEntityRenderer
        implements BlockEntityRenderer<ManathiefBlockEntity> {
    public static final SpriteIdentifier BOOK_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("entity/enchanting_table_book"));
    private final ManathiefFaceModel face;

    public ManathiefBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.face = new ManathiefFaceModel(ctx.getLayerModelPart(EntityModelLayers.BOOK));
    }

    @Override
    public void render(ManathiefBlockEntity manathiefBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, Vec3d vec3d) {
        matrixStack.push();
        matrixStack.translate(0.5f, 0.75f, 0.5f);
        float g = (float) manathiefBlockEntity.ticks + f;
        matrixStack.translate(0.0f, 0.1f + MathHelper.sin(g * 0.1f) * 0.01f, 0.0f);
        float h2 = ManathiefBlockEntity.normalizeRotation(manathiefBlockEntity.bookRotation - manathiefBlockEntity.lastBookRotation);
        float k = manathiefBlockEntity.lastBookRotation + h2 * f;
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(-k));
        ////matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(80.0f));
        //matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-p));
        this.face.setPageAngles(g, 1, 0, 1);
        VertexConsumer vertexConsumer = BOOK_TEXTURE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntitySolid);
        this.face.render(matrixStack, vertexConsumer, i, j);
        matrixStack.pop();
    }
}

