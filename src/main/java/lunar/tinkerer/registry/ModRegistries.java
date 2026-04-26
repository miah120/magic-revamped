package lunar.tinkerer.registry;

import lunar.tinkerer.consequences.Consequence;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class ModRegistries {
    public static final Registry<Consequence> CONSEQUENCE;

    static {
        CONSEQUENCE = create(ModRegistryKeys.CONSEQUENCE);
    }

    private static <T> Registry<T> create(ResourceKey<Registry<T>> registryKey) {
        return FabricRegistryBuilder.create(registryKey).buildAndRegister();
    }
}
