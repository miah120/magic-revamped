package lunar.tinkerer.consequences;

import lunar.tinkerer.registry.ModRegistryKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;

public class ConsequenceManager {
    public static Consequence pick(Level world, List<BlockPos> area) {
        List<BlockInWorld> blocks = area.stream()
             .map(blockPos -> new BlockInWorld(world, blockPos, false))
             .toList();
        List<Consequence> consequenceList = world.registryAccess()
            .lookupOrThrow(ModRegistryKeys.CONSEQUENCE).stream()
            .filter(consequence -> blocks.stream().anyMatch(consequence::test))
            .toList();
        return WeightedRandom.getRandomItem(world.getRandom(), consequenceList, Consequence::weight).orElse(Consequence.EMPTY);
    }
}
