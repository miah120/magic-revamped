package lunar.tinkerer.consequences;

import lunar.tinkerer.registry.ModRegistryKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;
import java.util.Optional;

public class ConsequenceManager {
    public static Info pick(Level world, List<BlockPos> area) {
        List<BlockInWorld> blocks = area.stream()
             .map(blockPos -> new BlockInWorld(world, blockPos, false))
             .toList();
        List<Info> consequenceList = world.registryAccess()
            .lookupOrThrow(ModRegistryKeys.CONSEQUENCE).stream()
            .flatMap(consequence -> blocks.stream()
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
