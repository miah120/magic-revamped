package lunar.tinkerer.Consequences;

import lunar.tinkerer.registry.ModRegistryKeys;
import net.minecraft.world.World;

public class ConsequenceManager {
    public static Consequence pick(World world) {
        return world.getRegistryManager()
                .getOrThrow(ModRegistryKeys.CONSEQUENCE)
                .get(0);
    }
}
