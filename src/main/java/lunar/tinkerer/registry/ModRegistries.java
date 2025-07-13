package lunar.tinkerer.registry;

import lunar.tinkerer.consequences.Consequence;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class ModRegistries {
    public static final Registry<Consequence> CONSEQUENCE;

    static {
        CONSEQUENCE = create(ModRegistryKeys.CONSEQUENCE);
    }

    private static <T> Registry<T> create(RegistryKey<Registry<T>> registryKey) {
        return FabricRegistryBuilder.createSimple(registryKey).buildAndRegister();
    }
}
