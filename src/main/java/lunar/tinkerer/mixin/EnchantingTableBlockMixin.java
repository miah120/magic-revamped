package lunar.tinkerer.mixin;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.enchantingTable.ModEnchantmentScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {
    @Inject(method = "getMenuProvider", at = @At(value = "HEAD"), cancellable = true)
    protected void getMenuProvider(BlockState state, Level world, BlockPos pos, CallbackInfoReturnable<MenuProvider> ci) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof EnchantingTableBlockEntity enchantingTable) {
            Component text = enchantingTable.getDisplayName();
            ci.setReturnValue(new SimpleMenuProvider((syncId, inventory, player) -> new ModEnchantmentScreenHandler(syncId, inventory, ContainerLevelAccess.create(world, pos)), text));
        } else {
            ci.setReturnValue(null);
        }
        ci.cancel();
    }

    @Inject(method = "animateTick", at = @At(value = "HEAD"), cancellable = true)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        for (BlockPos blockPos : MagicRevamped.POWER_PROVIDER_OFFSETS) {
            if (random.nextInt(12) != 0 || !EnchantingTableBlock.isValidBookShelf(world, pos, blockPos)) continue;
            world.addParticle(ParticleTypes.ENCHANT, (double)pos.getX() + 0.5, (double)pos.getY() + 2.0, (double)pos.getZ() + 0.5, (double)((float)blockPos.getX() + random.nextFloat()) - 0.5, (float)blockPos.getY() - random.nextFloat() - 1.0f, (double)((float)blockPos.getZ() + random.nextFloat()) - 0.5);
        }
        ci.cancel();
    }
}
