package lunar.tinkerer;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class MagicRevampedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.MANATHIEF_FACE_MODEL_LAYER, ManathiefFaceModel::createBodyLayer);
		BlockEntityRenderers.register(ModBlockEntities.ENCHANTING_TABLE_BLOCK_ENTITY, ModEnchantingTableBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.MANATHIEF_BLOCK_ENTITY, ManathiefBlockEntityRenderer::new);
		MenuScreens.register(
				ModBlockEntities.ENCHANTMENT_SCREEN_HANDLER,
				ModEnchantmentScreen::new
		);
		BlockRenderLayerMap.putBlock(ModBlocks.MANATHIEF, ChunkSectionLayer.CUTOUT);
		ParticleFactoryRegistry.getInstance().register(MagicRevamped.BREAK_ENCHANT_PARTICLE, BreakEnchantParticle.Factory::new);
	}
}