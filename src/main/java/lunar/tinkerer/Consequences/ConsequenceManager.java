package lunar.tinkerer.Consequences;

import lunar.tinkerer.registry.ModRegistryKeys;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ConsequenceManager {
    public static Consequence pick(World world, List<BlockPos> area) {
        List<Block> blocks = area.stream()
             .map(world::getBlockState)
             .map(AbstractBlock.AbstractBlockState::getBlock)
             .toList();
        List<Consequence> consequenceList = world.getRegistryManager()
            .getOrThrow(ModRegistryKeys.CONSEQUENCE).stream()
            .filter(consequence -> blocks.stream().anyMatch(consequence::test))
            .toList();
        if(consequenceList.isEmpty()) return ConsequenceRegistry.DEFAULT;
        return consequenceList.get(world.random.nextInt(consequenceList.size()));
    }
}
