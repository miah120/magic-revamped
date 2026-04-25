package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import lunar.tinkerer.enchantingTable.ModEnchantingTableBlock;
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
    @Override
    public ItemStack run(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        List<BlockPos> targets = ModEnchantingTableBlock.DECORATION_OFFSETS.stream()
            .map(blockPos1 -> blockPos1.offset(blockPos))
            .filter(blockPos1 -> this.test(world.getBlockState(blockPos1).getBlock()))
            .toList();
        BlockPos target = targets.get(world.random.nextInt(targets.size()));
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
