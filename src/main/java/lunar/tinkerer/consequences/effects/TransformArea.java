package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.stream.Stream;

public record TransformArea(Vec3i start, Vec3i end, BlockState result, BlockState from) implements ConsequenceEffect {
    @Override
    public ItemStack run(ServerWorld world, BlockPos blockPos, ServerPlayerEntity player, RecipeInputInventory input, ItemStack stack) {
        getBlockPositions(start, end, blockPos).forEach(pos -> {
            if(from != null && world.getBlockState(pos) != from) {
                return;
            }
            world.setBlockState(pos, result);
        });
        return ItemStack.EMPTY;
    }

    public Stream<BlockPos> getBlockPositions(Vec3i start, Vec3i end, BlockPos blockPos) {
        var relStart = blockPos.add(start);
        var relEnd = blockPos.add(end);
        return BlockPos.stream(relStart, relEnd);
    }
}
