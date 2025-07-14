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
        List.of((world, blockPos, player, input) -> ItemStack.EMPTY),
        true,
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

    //    public static final MapCodec<Consequence> CODEC = RecordCodecBuilder
    //        .mapCodec(instance -> instance.group(
    //                Codec.STRING
    //                        .optionalFieldOf("description", "")
    //                        .forGetter(consequence -> consequence.description),
    //                Ingredient.CODEC
    //                    .fieldOf("decoration")
    //                    .forGetter(consequence -> consequence.decoration),
    //                Ingredient.CODEC
    //                    .fieldOf("decoration")
    //                    .forGetter(consequence -> consequence.decoration),
    //                Codec.INT
    //                    .fieldOf("weight")
    //                    .forGetter(consequence -> consequence.weight)
    //                //TODO: Implement consequence effects
    //            ).apply(instance, Consequence::new)
    //        );
    //
    //    public static final Codec<RegistryEntry<Consequence>> ENTRY_CODEC = RegistryFixedCodec
    //            .of(ModRegistryKeys.CONSEQUENCE);
    //    public static final PacketCodec<RegistryByteBuf, RegistryEntry<Consequence>> ENTRY_PACKET_CODEC = PacketCodecs
    //        .registryEntry(ModRegistryKeys.CONSEQUENCE);

    public boolean test(Block block) {
        return this.decoration.test(new ItemStack(block.asItem()));
    }
}
