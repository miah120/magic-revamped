package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.Optional;

public record TransformBlock(BlockPredicate target, BlockState result, Boolean dropResources, Boolean targetDecoration)
        implements ConsequenceEffect {
    public static MapCodec<TransformBlock> CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
            BlockPredicate.CODEC.optionalFieldOf("target", BlockPredicate.Builder.block().build()).forGetter(TransformBlock::target),
            BlockState.CODEC.fieldOf("result").forGetter(TransformBlock::result),
            Codec.BOOL.optionalFieldOf("drop_resources", true).forGetter(TransformBlock::dropResources),
            Codec.BOOL.optionalFieldOf("target_decoration", false).forGetter(TransformBlock::targetDecoration)
        ).apply(i, TransformBlock::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
        this.getTarget(info)
            .map(BlockInWorld::getPos)
            .ifPresent(target -> {
                info.world().destroyBlock(target, this.dropResources);
                if (result.getBlock() instanceof DoublePlantBlock) {
                    DoublePlantBlock.placeAt(info.world(), result, target, 2);
                } else {
                    info.world().setBlockAndUpdate(target, this.result);
                }
            });
        return ItemStack.EMPTY;
    }

    public Optional<BlockInWorld> getTarget(Consequence.RunInfo info) {
        if (targetDecoration) return info.decoration();
        return Util.getRandomSafe(MagicRevamped.DECORATION_OFFSETS.stream()
                .map(blockPos1 -> blockPos1.offset(info.tablePos()))
                .map(pos -> new BlockInWorld(info.world(), pos, false))
                .filter(this::test)
                .toList(),
            info.world().getRandom()
        );
    }

    public boolean test(BlockInWorld block) {
        return this.target.matches(block);
    }

}
