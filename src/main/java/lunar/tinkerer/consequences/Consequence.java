package lunar.tinkerer.consequences;

import net.minecraft.block.Block;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.stream.IntStream;


public record Consequence(
        String description,
        Ingredient decoration,
        List<ConsequenceEffect> effectList,
        Boolean succeeds,
        Integer weight
) {
    public static final Consequence EMPTY = new Consequence(
        "default",
        Ingredient.ofItem(Items.BARRIER),
        List.of((world, blockPos, player, input) -> {
            IntStream.range(1, input.size()).forEach(
                i -> input.removeStack(i, 1)
            );
            return ItemStack.EMPTY;
        }),
        false,
        0
    );
    public record Result<T> (T entry, boolean success) {}

    public Result<ItemStack> run(ServerWorld world, BlockPos blockPos, ServerPlayerEntity player, RecipeInputInventory input) {
        var results = effectList.stream()
            .map(effect -> effect.run(world, blockPos, player, input))
            .toList();
        return new Result<>(
            results.isEmpty() ? ItemStack.EMPTY : results.getFirst(),
            this.succeeds
        );
    }

    public boolean test(Block block) {
        return this.decoration.test(new ItemStack(block.asItem()));
    }
}
