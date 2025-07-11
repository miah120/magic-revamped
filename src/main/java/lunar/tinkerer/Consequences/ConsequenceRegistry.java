package lunar.tinkerer.Consequences;

import lunar.tinkerer.MagicRevamped;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class ConsequenceRegistry {
    public static final RegistryKey<Registry<Consequence>> CONSEQUENCE = RegistryKey
            .ofRegistry(MagicRevamped.identifier("consequence"));
}
