package lunar.tinkerer.mixin;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.consequences.Consequence;
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
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;
import java.util.stream.IntStream;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {
    @Inject(method = "getMenuProvider", at = @At(value = "HEAD"), cancellable = true)
    protected void getMenuProvider(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<MenuProvider> ci) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnchantingTableBlockEntity enchantingTable) {
            Component text = enchantingTable.getDisplayName();
            ci.setReturnValue(new SimpleMenuProvider((syncId, inventory, _) -> new ModEnchantmentScreenHandler(syncId, inventory, ContainerLevelAccess.create(level, pos)), text));
        } else {
            ci.setReturnValue(null);
        }
        ci.cancel();
    }

    @Inject(method = "animateTick", at = @At(value = "HEAD"), cancellable = true)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        bookshelfParticles(level, pos, random);
        try { decorationParticles(level, pos, random); } catch (Exception _) {}
        ci.cancel();
    }

    @Unique
    private void bookshelfParticles(Level world, BlockPos pos, RandomSource random) {
        for (BlockPos blockPos : MagicRevamped.POWER_PROVIDER_OFFSETS) {
            if (random.nextInt(12) != 0 || !EnchantingTableBlock.isValidBookShelf(world, pos, blockPos)) continue;
            Vec3 pos1 = new Vec3(pos).add(0.5, 2.0, 0.5);
            Vec3 pos2 = new Vec3(blockPos).add(random.nextFloat() - 0.5, -random.nextFloat() - 1.0, random.nextFloat() - 0.5);
            world.addParticle(ParticleTypes.ENCHANT, pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
        }
    }

    @Unique
    private void decorationParticles(Level world, BlockPos pos, RandomSource random) {
        if (random.nextInt(24) != 0) return;
        Consequence.pick(world, pos).getB().ifPresent(blockInWorld -> {
            Supplier<Vec3> pos1 = () -> new Vec3(blockInWorld.getPos()).add(
                1.5 * random.nextFloat() - 0.25,
                1.5 * random.nextFloat() - 0.25,
                1.5 * random.nextFloat() - 0.25
            );
            Supplier<Vec3> vel = () -> new Vec3(pos)
                .subtract(blockInWorld.getPos().getX(), blockInWorld.getPos().getY(), blockInWorld.getPos().getZ())
                .add(random.nextFloat() - 0.5, random.nextFloat() - 0.5, random.nextFloat() - 0.5)
                .normalize()
                .scale(0.05);

            IntStream.range(0, 5).forEach(_ -> world.addParticle(
                ParticleTypes.END_ROD, pos1.get().x, pos1.get().y, pos1.get().z, vel.get().x, vel.get().y, vel.get().z)
            );
        });
    }
}
