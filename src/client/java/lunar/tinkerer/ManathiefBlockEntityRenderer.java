package lunar.tinkerer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ManathiefBlockEntityRenderer
        implements BlockEntityRenderer<ManathiefBlockEntity, ManathiefBlockEntityRenderState> {
    //TODO: Make this work
    private static final SpriteIdentifier TEXTURE = TexturedRenderLayers.ENTITY_SPRITE_MAPPER.map(MagicRevamped.identifier("manathief_face"));
    private final ManathiefFaceModel face;
    private final SpriteHolder spriteHolder;

    public ManathiefBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.spriteHolder = ctx.spriteHolder();
        this.face = new ManathiefFaceModel(ctx.getLayerModelPart(ModModelLayers.MANATHIEF_FACE_MODEL_LAYER));
    }

    public ManathiefBlockEntityRenderState createRenderState() {
        return new ManathiefBlockEntityRenderState();
    }

    public void updateRenderState(
            ManathiefBlockEntity manathiefBlockEntity,
            ManathiefBlockEntityRenderState manathiefBlockEntityRenderState,
            float f,
            Vec3d vec3d,
            @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlayCommand
    ) {
        BlockEntityRenderer.super.updateRenderState(manathiefBlockEntity, manathiefBlockEntityRenderState, f, vec3d, crumblingOverlayCommand);
        manathiefBlockEntityRenderState.ticks = manathiefBlockEntity.ticks + f;
        float g = ManathiefBlockEntity.normalizeRotation(manathiefBlockEntity.bookRotation - manathiefBlockEntity.lastBookRotation);
        manathiefBlockEntityRenderState.bookRotationDegrees = manathiefBlockEntity.lastBookRotation + (g * f);
    }

    public void render(
            ManathiefBlockEntityRenderState manathiefBlockEntityRenderState,
            MatrixStack matrixStack,
            OrderedRenderCommandQueue orderedRenderCommandQueue,
            CameraRenderState cameraRenderState
    ) {
        matrixStack.push();
        matrixStack.translate(0.5F, 0.75F, 0.5F);
        matrixStack.translate(0.0F, 0.1F + MathHelper.sin(manathiefBlockEntityRenderState.ticks * 0.1F) * 0.01F, 0.0F);

        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(-manathiefBlockEntityRenderState.bookRotationDegrees));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-130));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(60.0f));

        ManathiefFaceModel.ManathiefFaceModelState faceModelState = new ManathiefFaceModel.ManathiefFaceModelState();
        orderedRenderCommandQueue.submitModel(
                this.face,
                faceModelState,
                matrixStack,
                TEXTURE.getRenderLayer(RenderLayers::entityCutout),
                manathiefBlockEntityRenderState.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                -1,
                this.spriteHolder.getSprite(TEXTURE),
                0,
                manathiefBlockEntityRenderState.crumblingOverlay
        );
        matrixStack.pop();
    }
}

