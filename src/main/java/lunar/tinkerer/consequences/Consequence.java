package lunar.tinkerer.consequences;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.util.Tuple;
import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public record Consequence(
        BlockPredicate decoration,
        List<ConsequenceEffect> effectList,
        Boolean succeeds,
        Integer weight,
        Boolean preserveDecoration
) {
    public static final Codec<Consequence> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BlockPredicate.CODEC.fieldOf("decoration").forGetter(Consequence::decoration),
            ConsequenceEffect.CODEC.listOf().fieldOf("effects").forGetter(Consequence::effectList),
            Codec.BOOL.optionalFieldOf("succeeds", false).forGetter(Consequence::succeeds),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("weight", 1).forGetter(Consequence::weight),
            Codec.BOOL.optionalFieldOf("preserve_decoration", false).forGetter(Consequence::preserveDecoration)
        ).apply(instance, Consequence::new)
    );

    public static final Consequence EMPTY = new Consequence(
        BlockPredicate.Builder.block().build(),
        List.of(new ConsequenceEffect() {
            @Override
            public MapCodec<? extends ConsequenceEffect> codec() { return null; }

            @Override
            public ItemStack apply(Consequence.RunInfo info) {
                IntStream.range(1, info.input.getContainerSize()).forEach(i -> info.input.removeItem(i, 1));
                return ItemStack.EMPTY;
            }
        }),
        false,
        0,
        false
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
        BlockPos tablePos,
        ServerPlayer player,
        CraftingContainer input,
        ItemStack stack,
        Optional<BlockInWorld> decoration
    ) {}

    public boolean test(BlockInWorld blockInWorld) {
        return this.decoration.matches(blockInWorld);
    }

    public static void init() {}

    public static Info pick(Level world, BlockPos pos) {
        List<Info> consequenceList = world.registryAccess()
            .lookupOrThrow(MagicRevamped.RegistryKeys.CONSEQUENCE).stream()
            .flatMap(consequence -> MagicRevamped.DECORATION_OFFSETS.stream()
                .map(pos::offset)
                .map(blockPos -> new BlockInWorld(world, blockPos, false))
                .filter(consequence::test)
                .map(block -> new Info(consequence, block))
            )
            .toList();
        return WeightedRandom
            .getRandomItem(world.getRandom(), consequenceList, t -> t.getA().weight())
            .orElse(Info.EMPTY);
    }

    public static class Info extends Tuple<Consequence, Optional<BlockInWorld>> {
        public static Info EMPTY = new Info(Consequence.EMPTY, Optional.empty());

        public Info(Consequence consequence, Optional<BlockInWorld> blockInWorld) {
            super(consequence, blockInWorld);
        }

        public Info(Consequence consequence, BlockInWorld blockInWorld) {
            super(consequence, Optional.of(blockInWorld));
        }

        public Consequence.Result<ItemStack> run(
            ServerLevel world,
            BlockPos blockPos,
            ServerPlayer player,
            CraftingContainer input,
            ItemStack stack
        ) {
            Consequence.Result<ItemStack> result = this.getA().run(new Consequence.RunInfo(
                world,
                blockPos,
                player,
                input,
                stack,
                this.getB()
            ));
            if (!this.getA().preserveDecoration()) {
                this.getB().ifPresent(block -> world.destroyBlock(block.getPos(), false));
            }
            return result;
        }
    }
}
