package lunar.tinkerer.registry;

import lunar.tinkerer.consequences.Consequence;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import lunar.tinkerer.MagicRevamped;

public class ModRegistryKeys {
    public static final ResourceKey<Registry<Consequence>> CONSEQUENCE;

    static {
        CONSEQUENCE = create("consequence");
    }

    private static <T> ResourceKey<Registry<T>> create(String path) {
        return ResourceKey.createRegistryKey(MagicRevamped.identifier(path));
    }
}
