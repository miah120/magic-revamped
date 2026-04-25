package lunar.tinkerer.consequences;

import lunar.tinkerer.registry.ModRegistryKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.List;

public class ConsequenceManager {
    public static Consequence pick(Level world, List<BlockPos> area) {
        List<Block> blocks = area.stream()
             .map(world::getBlockState)
             .map(BlockBehaviour.BlockStateBase::getBlock)
             .toList();
        List<Consequence> consequenceList = world.registryAccess()
            .lookupOrThrow(ModRegistryKeys.CONSEQUENCE).stream()
            .filter(consequence -> blocks.stream().anyMatch(consequence::test))
            .toList();
        return WeightedRandom.getRandomItem(world.random, consequenceList, Consequence::weight).orElse(ConsequenceRegistry.DEFAULT);
    }
}
