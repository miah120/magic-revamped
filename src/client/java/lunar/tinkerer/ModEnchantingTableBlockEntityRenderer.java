package lunar.tinkerer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lunar.tinkerer.enchantingTable.ModEnchantingTableBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
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
public class ModEnchantingTableBlockEntityRenderer implements BlockEntityRenderer<ModEnchantingTableBlockEntity, EnchantTableRenderState> {
    public static final Material BOOK_TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("enchanting_table_book");
    private final MaterialSet materialSet;
    private final BookModel book;

    public ModEnchantingTableBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.materialSet = ctx.materials();
        this.book = new BookModel(ctx.bakeLayer(ModelLayers.BOOK));
    }

    public EnchantTableRenderState createRenderState() {
        return new EnchantTableRenderState();
    }

    public void extractRenderState(
            ModEnchantingTableBlockEntity enchantingTableBlockEntity,
            EnchantTableRenderState enchantTableRenderState,
            float f,
            @NonNull Vec3 vec3d,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlayCommand
    ) {
        BlockEntityRenderer.super.extractRenderState(enchantingTableBlockEntity, enchantTableRenderState, f, vec3d, crumblingOverlayCommand);
        enchantTableRenderState.flip = Mth.lerp(f, enchantingTableBlockEntity.pageAngle, enchantingTableBlockEntity.nextPageAngle);
        enchantTableRenderState.open = Mth.lerp(
                f, enchantingTableBlockEntity.pageTurningSpeed, enchantingTableBlockEntity.nextPageTurningSpeed
        );
        enchantTableRenderState.time = enchantingTableBlockEntity.ticks + f;
        float g = enchantingTableBlockEntity.bookRotation - enchantingTableBlockEntity.lastBookRotation;

        while (g >= (float) Math.PI) {
            g -= (float) (Math.PI * 2);
        }

        while (g < (float) -Math.PI) {
            g += (float) (Math.PI * 2);
        }

        enchantTableRenderState.yRot = enchantingTableBlockEntity.lastBookRotation + g * f;
    }

    public void submit(
            EnchantTableRenderState enchantTableRenderState,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector submitNodeCollector,
            @NonNull CameraRenderState cameraRenderState
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        poseStack.translate(0.0F, 0.1F + Mth.sin(enchantTableRenderState.time * 0.1F) * 0.01F, 0.0F);
        float f = enchantTableRenderState.yRot;
        poseStack.mulPose(Axis.YP.rotation(-f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        BookModel.State bookModelState = getBookModelState(enchantTableRenderState);
        submitNodeCollector.submitModel(
                this.book,
                bookModelState,
                poseStack,
                BOOK_TEXTURE.renderType(RenderTypes::entitySolid),
                enchantTableRenderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                this.materialSet.get(BOOK_TEXTURE),
                0,
                enchantTableRenderState.breakProgress
        );
        poseStack.popPose();
    }

    private static BookModel.State getBookModelState(EnchantTableRenderState enchantTableRenderState) {
        float g = Mth.frac(enchantTableRenderState.flip + 0.25F) * 1.6F - 0.3F;
        float h = Mth.frac(enchantTableRenderState.flip + 0.75F) * 1.6F - 0.3F;
        return new BookModel.State(
                enchantTableRenderState.time,
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(h, 0.0F, 1.0F),
                enchantTableRenderState.open
        );
    }
}

