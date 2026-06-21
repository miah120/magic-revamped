package lunar.tinkerer.consequences;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


public record Consequence(
        BlockPredicate decoration,
        List<ConsequenceEffect> effectList,
        Boolean succeeds,
        Integer weight
) {
    public static final Codec<Consequence> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPredicate.CODEC.fieldOf("decoration").forGetter(Consequence::decoration),
                    ConsequenceEffect.CODEC.listOf().fieldOf("effects").forGetter(Consequence::effectList),
                    Codec.BOOL.optionalFieldOf("succeeds", false).forGetter(Consequence::succeeds),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("weight", 1).forGetter(Consequence::weight)
            ).apply(instance, Consequence::new)
    );


    public static final Consequence EMPTY = new Consequence(
        BlockPredicate.Builder.block().of(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK), Blocks.BARRIER).build(),
        List.of(new ConsequenceEffect() {
            @Override
            public MapCodec<? extends ConsequenceEffect> codec() {
                return null;
            }

            @Override
            public ItemStack apply(Consequence.RunInfo info) {
                IntStream.range(1, info.input.getContainerSize()).forEach(
                        i -> info.input.removeItem(i, 1)
                );
                return ItemStack.EMPTY;
            }
        }),
        false,
        0
    );
    public record Result<T> (T entry, boolean success, boolean decorationsPresent) {}

    public Result<ItemStack> run(RunInfo info) {
        List<ItemStack> results;
        try {
            results = effectList.stream()
                .map(effect -> effect.apply(info))
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

    public record RunInfo(
        ServerLevel world,
        BlockPos blockPos,
        ServerPlayer player,
        CraftingContainer input,
        ItemStack stack,
        Optional<BlockInWorld> decoration
    ) {}

    public boolean test(BlockInWorld blockInWorld) {
        return this.decoration.matches(blockInWorld);
    }

    public static void init() {}
}
