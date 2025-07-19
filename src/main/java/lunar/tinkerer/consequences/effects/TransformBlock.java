package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import lunar.tinkerer.enchantingTable.ModEnchantingTableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record TransformBlock(Ingredient target, BlockState result) implements ConsequenceEffect {
    @Override
    public ItemStack run(ServerWorld world, BlockPos blockPos, ServerPlayerEntity player, RecipeInputInventory input, ItemStack stack) {
        List<BlockPos> targets = ModEnchantingTableBlock.DECORATION_OFFSETS.stream()
            .map(blockPos1 -> blockPos1.add(blockPos))
            .filter(blockPos1 -> this.test(world.getBlockState(blockPos1).getBlock()))
            .toList();
        BlockPos target = targets.get(world.random.nextInt(targets.size()));
        world.setBlockState(target, this.result);
        return ItemStack.EMPTY;
    }

    public boolean test(Block block) {
        return this.target.test(new ItemStack(block.asItem()));
    }

}
