package lunar.tinkerer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@Environment(value=EnvType.CLIENT)
public class ManathiefBlockEntityRenderer
        implements BlockEntityRenderer<ManathiefBlockEntity, ManathiefBlockEntityRenderState> {
    //TODO: Make this work
    private static final Material TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.apply(MagicRevamped.identifier("manathief_face"));
    private final ManathiefFaceModel face;
    private final MaterialSet spriteHolder;

    public ManathiefBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.spriteHolder = ctx.materials();
        this.face = new ManathiefFaceModel(ctx.bakeLayer(ModModelLayers.MANATHIEF_FACE_MODEL_LAYER));
    }

    public ManathiefBlockEntityRenderState createRenderState() {
        return new ManathiefBlockEntityRenderState();
    }

    public void extractRenderState(
            ManathiefBlockEntity manathiefBlockEntity,
            ManathiefBlockEntityRenderState manathiefBlockEntityRenderState,
            float f,
            @NonNull Vec3 vec3d,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlayCommand
    ) {
        BlockEntityRenderer.super.extractRenderState(manathiefBlockEntity, manathiefBlockEntityRenderState, f, vec3d, crumblingOverlayCommand);
        manathiefBlockEntityRenderState.ticks = manathiefBlockEntity.ticks + f;
        float g = ManathiefBlockEntity.normalizeRotation(manathiefBlockEntity.bookRotation - manathiefBlockEntity.lastBookRotation);
        manathiefBlockEntityRenderState.bookRotationDegrees = manathiefBlockEntity.lastBookRotation + (g * f);
    }

    public void submit(
            ManathiefBlockEntityRenderState manathiefBlockEntityRenderState,
            PoseStack matrixStack,
            SubmitNodeCollector orderedRenderCommandQueue,
            @NonNull CameraRenderState cameraRenderState
    ) {
        matrixStack.pushPose();
        matrixStack.translate(0.5F, 0.75F, 0.5F);
        matrixStack.translate(0.0F, 0.1F + Mth.sin(manathiefBlockEntityRenderState.ticks * 0.1F) * 0.01F, 0.0F);

        matrixStack.mulPose(Axis.YP.rotation(-manathiefBlockEntityRenderState.bookRotationDegrees));
        matrixStack.mulPose(Axis.YP.rotationDegrees(-130));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(60.0f));

        ManathiefFaceModel.ManathiefFaceModelState faceModelState = new ManathiefFaceModel.ManathiefFaceModelState();
        orderedRenderCommandQueue.submitModel(
                this.face,
                faceModelState,
                matrixStack,
                TEXTURE.renderType(RenderTypes::entityCutout),
                manathiefBlockEntityRenderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                this.spriteHolder.get(TEXTURE),
                0,
                manathiefBlockEntityRenderState.breakProgress
        );
        matrixStack.popPose();
    }
}

