package lunar.tinkerer.registry;

import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModRegistries {
    public static final Registry<MapCodec<? extends ConsequenceEffect>> CONSEQUENCE_EFFECT = create(ModRegistryKeys.CONSEQUENCE_EFFECT, ConsequenceEffect::bootstrap);
//    public static final Registry<Consequence> CONSEQUENCE = create(ModRegistryKeys.CONSEQUENCE, Consequence::bootstrap);

    public static void init() {
        DynamicRegistries.register(ModRegistryKeys.CONSEQUENCE, Consequence.CODEC);
    }

    private static <T> Registry<T> create(ResourceKey<Registry<T>> registryKey, Function<Registry<T>, Object> loader) {
        Registry<T> registry = FabricRegistryBuilder.create(registryKey).buildAndRegister();
        loader.apply(registry);
        return registry;
    }
}
