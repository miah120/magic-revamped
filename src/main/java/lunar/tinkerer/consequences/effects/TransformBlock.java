package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;

public record TransformBlock(BlockPredicate target, BlockState result) implements ConsequenceEffect {
    public static MapCodec<TransformBlock> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    BlockPredicate.CODEC.fieldOf("target").forGetter(TransformBlock::target),
                    BlockState.CODEC.fieldOf("result").forGetter(TransformBlock::result)
            ).apply(i, TransformBlock::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
        List<BlockInWorld> targets = MagicRevamped.DECORATION_OFFSETS.stream()
            .map(blockPos1 -> blockPos1.offset(info.blockPos()))
            .map(pos -> new BlockInWorld(info.world(), pos, false))
            .filter(this::test)
            .toList();
        if (targets.isEmpty()) return ItemStack.EMPTY;
        BlockPos target = targets.get(info.world().getRandom().nextInt(targets.size())).getPos();
        if (result.getBlock() instanceof DoublePlantBlock) {
            DoublePlantBlock.placeAt(info.world(), result, info.blockPos(), 2);
        } else {
            info.world().setBlockAndUpdate(target, this.result);
        }
        return ItemStack.EMPTY;
    }

    public boolean test(BlockInWorld block) {
        return this.target.matches(block);
    }

}
