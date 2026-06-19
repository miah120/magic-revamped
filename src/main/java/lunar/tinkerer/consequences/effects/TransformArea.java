package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.stream.Stream;

public record TransformArea(Vec3i start, Vec3i end, BlockState result, Optional<BlockPredicate> from) implements ConsequenceEffect {
    public static MapCodec<TransformArea> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Vec3i.CODEC.fieldOf("start_offset").forGetter(TransformArea::start),
                    Vec3i.CODEC.fieldOf("end_offset").forGetter(TransformArea::end),
                    BlockState.CODEC.fieldOf("result").forGetter(TransformArea::result),
                    BlockPredicate.CODEC.optionalFieldOf("only_transform").forGetter(TransformArea::from)
            ).apply(i, TransformArea::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        getBlockPositions(start, end, blockPos)
            .filter(pos -> from.filter(f -> !f.matches(world, pos)).isEmpty())
            .forEach(pos -> world.setBlockAndUpdate(pos, result));
        return ItemStack.EMPTY;
    }

    public Stream<BlockPos> getBlockPositions(Vec3i start, Vec3i end, BlockPos blockPos) {
        var relStart = blockPos.offset(start);
        var relEnd = blockPos.offset(end);
        return BlockPos.betweenClosedStream(relStart, relEnd);
    }
}
