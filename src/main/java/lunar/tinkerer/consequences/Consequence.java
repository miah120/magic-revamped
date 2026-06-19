package lunar.tinkerer.consequences;

import java.util.List;
import java.util.stream.IntStream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;


public record Consequence(
        Ingredient decoration,
        List<ConsequenceEffect> effectList,
        Boolean succeeds,
        Integer weight
) {
    public static final Codec<Consequence> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    //TODO: This should be a blockstate instead
                    Ingredient.CODEC.fieldOf("decoration").forGetter(Consequence::decoration),
                    ConsequenceEffect.CODEC.listOf().fieldOf("effects").forGetter(Consequence::effectList),
                    Codec.BOOL.optionalFieldOf("succeeds", false).forGetter(Consequence::succeeds),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("weight", 1).forGetter(Consequence::weight)
            ).apply(instance, Consequence::new)
    );


    public static final Consequence EMPTY = new Consequence(
        Ingredient.of(Items.BARRIER),
        List.of(new ConsequenceEffect() {
            @Override
            public MapCodec<? extends ConsequenceEffect> codec() {
                return null;
            }

            @Override
            public ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
                IntStream.range(1, input.getContainerSize()).forEach(
                        i -> input.removeItem(i, 1)
                );
                return ItemStack.EMPTY;
            }
        }),
        false,
        0
    );
    public record Result<T> (T entry, boolean success, boolean decorationsPresent) {}

    public Result<ItemStack> run(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        List<ItemStack> results;
        try {
            results = effectList.stream()
                .map(effect -> effect.apply(world, blockPos, player, input, stack))
                .toList();
        } catch (Exception _) {
            results = List.of();
        }
        return new Result<>(
            results.isEmpty() ? ItemStack.EMPTY : results.getFirst(),
            this.succeeds,
            this != Consequence.EMPTY
        );
    }

    public boolean test(Block block) {
        return this.decoration.test(new ItemStack(block.asItem()));
    }

    public static void init() {}
}
