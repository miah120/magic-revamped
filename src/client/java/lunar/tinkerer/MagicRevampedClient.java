package lunar.tinkerer;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class MagicRevampedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererFactories.register(ModBlockEntities.ENCHANTING_TABLE_BLOCK_ENTITY, ModEnchantingTableBlockEntityRenderer::new);
	}
}