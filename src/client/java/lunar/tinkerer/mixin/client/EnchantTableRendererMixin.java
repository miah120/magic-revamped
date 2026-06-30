package lunar.tinkerer.mixin.client;

import lunar.tinkerer.MagicRevamped;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EnchantTableRenderer.class)
public class EnchantTableRendererMixin {
    @Unique
    public boolean shouldRender(BlockEntity blockEntity, Vec3 cameraPosition) {
        EnchantTableRenderer thisObj = (EnchantTableRenderer) (Object) this;
        return blockEntity.getBlockState().getValue(BlockStateProperties.HAS_BOOK)
            && Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(cameraPosition, thisObj.getViewDistance());
    }
}
