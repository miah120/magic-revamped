package lunar.tinkerer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class MagicRevampedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ModelLayerRegistry.registerModelLayer(ModModelLayers.MANATHIEF_FACE_MODEL_LAYER, ManathiefFaceModel::createBodyLayer);
		BlockEntityRenderers.register(ModBlockEntities.MANATHIEF_BLOCK_ENTITY, ManathiefBlockEntityRenderer::new);
		MenuScreens.register(
				ModBlockEntities.ENCHANTMENT_SCREEN_HANDLER,
				ModEnchantmentScreen::new
		);
		ParticleProviderRegistry.getInstance().register(MagicRevamped.BREAK_ENCHANT_PARTICLE, BreakEnchantParticle.Factory::new);
	}
}