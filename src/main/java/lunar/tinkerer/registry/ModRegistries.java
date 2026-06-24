package lunar.tinkerer.registry;

import com.mojang.serialization.MapCodec;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;

public class ModRegistries {
    public static final Registry<MapCodec<? extends ConsequenceEffect>> CONSEQUENCE_EFFECT = create(ModRegistryKeys.CONSEQUENCE_EFFECT, ConsequenceEffect::bootstrap);

    public static void init() {
        DynamicRegistries.registerSynced(ModRegistryKeys.CONSEQUENCE, Consequence.CODEC);
    }

    private static <T> Registry<T> create(ResourceKey<Registry<T>> registryKey, Function<Registry<T>, Object> loader) {
        Registry<T> registry = FabricRegistryBuilder.create(registryKey).buildAndRegister();
        loader.apply(registry);
        return registry;
    }
}
