package lunar.tinkerer.Consequences;

import net.minecraft.world.World;

public class ConsequenceManager {
    public static Consequence pick(World world) {
        return world.getRegistryManager()
                .getOrThrow(ConsequenceRegistry.CONSEQUENCE)
                .get(0);
    }
}
