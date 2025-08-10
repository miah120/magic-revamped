package lunar.tinkerer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class MagicRevampedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.MANATHIEF_FACE_MODEL_LAYER, ManathiefFaceModel::getTexturedModelData);
		BlockEntityRendererFactories.register(ModBlockEntities.ENCHANTING_TABLE_BLOCK_ENTITY, ModEnchantingTableBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(ModBlockEntities.MANATHIEF_BLOCK_ENTITY, ManathiefBlockEntityRenderer::new);
		HandledScreens.register(
				ModBlockEntities.ENCHANTMENT_SCREEN_HANDLER,
				ModEnchantmentScreen::new
		);
		BlockRenderLayerMap.putBlock(ModBlocks.MANATHIEF, BlockRenderLayer.CUTOUT);
	}
}