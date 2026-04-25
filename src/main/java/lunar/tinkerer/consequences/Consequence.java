package lunar.tinkerer.consequences;

import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;


public record Consequence(
        String description,
        Ingredient decoration,
        List<ConsequenceEffect> effectList,
        Boolean succeeds,
        Integer weight
) {
    public static final Consequence EMPTY = new Consequence(
        "default",
        Ingredient.of(Items.BARRIER),
        List.of((world, blockPos, player, input, stack) -> {
            IntStream.range(1, input.getContainerSize()).forEach(
                i -> input.removeItem(i, 1)
            );
            return ItemStack.EMPTY;
        }),
        false,
        0
    );
    public record Result<T> (T entry, boolean success) {}

    public Result<ItemStack> run(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        var results = effectList.stream()
            .map(effect -> effect.run(world, blockPos, player, input, stack))
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
