package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record TransformDecoration(BlockState result, Boolean dropResources) implements ConsequenceEffect {
    public static MapCodec<TransformDecoration> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    BlockState.CODEC.fieldOf("result").forGetter(TransformDecoration::result),
                    Codec.BOOL.optionalFieldOf("drop_resources", false).forGetter(TransformDecoration::dropResources)
            ).apply(i, TransformDecoration::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
        info.decoration().map(BlockInWorld::getPos).ifPresent(target -> {
            info.world().destroyBlock(target, this.dropResources);
            if (result.getBlock() instanceof DoublePlantBlock) {
                DoublePlantBlock.placeAt(info.world(), result, info.blockPos(), 2);
            } else {
                info.world().setBlockAndUpdate(target, this.result);
            }
        });
        return ItemStack.EMPTY;
    }
}
