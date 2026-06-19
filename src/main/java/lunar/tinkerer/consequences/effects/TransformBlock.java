package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public record TransformBlock(Ingredient target, BlockState result) implements ConsequenceEffect {
    public static MapCodec<TransformBlock> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Ingredient.CODEC.fieldOf("target").forGetter(TransformBlock::target),
                    BlockState.CODEC.fieldOf("result").forGetter(TransformBlock::result)
            ).apply(i, TransformBlock::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        List<BlockPos> targets = MagicRevamped.DECORATION_OFFSETS.stream()
            .map(blockPos1 -> blockPos1.offset(blockPos))
            .filter(blockPos1 -> this.test(world.getBlockState(blockPos1).getBlock()))
            .toList();
        if (targets.isEmpty()) return ItemStack.EMPTY;
        BlockPos target = targets.get(world.getRandom().nextInt(targets.size()));
        if (result.getBlock() instanceof DoublePlantBlock) {
            DoublePlantBlock.placeAt(world, result, blockPos, 2);
        } else {
            world.setBlockAndUpdate(target, this.result);
        }
        return ItemStack.EMPTY;
    }

    public boolean test(Block block) {
        return this.target.test(new ItemStack(block.asItem()));
    }

}
