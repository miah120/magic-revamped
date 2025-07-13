package lunar.tinkerer.registry;

import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.MagicRevamped;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class ModRegistryKeys {
    public static final RegistryKey<Registry<Consequence>> CONSEQUENCE;

    static {
        CONSEQUENCE = create("consequence");
    }

    private static <T> RegistryKey<Registry<T>> create(String path) {
        return RegistryKey.ofRegistry(MagicRevamped.identifier(path));
    }
}
