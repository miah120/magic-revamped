package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.stream.Stream;

public record TransformArea(Vec3i start, Vec3i end, BlockState result, BlockState from) implements ConsequenceEffect {
    @Override
    public ItemStack run(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        getBlockPositions(start, end, blockPos).forEach(pos -> {
            if(from != null && world.getBlockState(pos) != from) {
                return;
            }
            world.setBlockAndUpdate(pos, result);
        });
        return ItemStack.EMPTY;
    }

    public Stream<BlockPos> getBlockPositions(Vec3i start, Vec3i end, BlockPos blockPos) {
        var relStart = blockPos.offset(start);
        var relEnd = blockPos.offset(end);
        return BlockPos.betweenClosedStream(relStart, relEnd);
    }
}
