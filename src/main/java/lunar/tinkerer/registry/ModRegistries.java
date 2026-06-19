package lunar.tinkerer.registry;

import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class ModRegistries {
    public static final Registry<Consequence> CONSEQUENCE = create(ModRegistryKeys.CONSEQUENCE);
    public static final Registry<ConsequenceEffect> CONSEQUENCE_EFFECT = create(ModRegistryKeys.CONSEQUENCE_EFFECT);

    public static void init() {
        DynamicRegistries.register(ModRegistryKeys.CONSEQUENCE, Consequence.CODEC);
    }

    private static <T> Registry<T> create(ResourceKey<Registry<T>> registryKey) {
        return FabricRegistryBuilder.create(registryKey).buildAndRegister();
    }
}
