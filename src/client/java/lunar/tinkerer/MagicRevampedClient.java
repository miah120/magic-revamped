package lunar.tinkerer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class MagicRevampedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ModelLayerRegistry.registerModelLayer(ModelLayers.MANATHIEF_FACE_MODEL_LAYER, ManathiefFaceModel::createBodyLayer);
		BlockEntityRenderers.register(MagicRevamped.BlockEntities.MANATHIEF_BLOCK_ENTITY, ManathiefBlockEntityRenderer::new);
		MenuScreens.register(MagicRevamped.ScreenHandlers.ENCHANTMENT_SCREEN_HANDLER, ModEnchantmentScreen::new);
		ParticleProviderRegistry.getInstance().register(MagicRevamped.BREAK_ENCHANT_PARTICLE, BreakEnchantParticle.Factory::new);
	}

	public static class ModelLayers {
		public static final ModelLayerLocation MANATHIEF_FACE_MODEL_LAYER = new ModelLayerLocation(MagicRevamped.id("manathief_face"), "main");
	}
}