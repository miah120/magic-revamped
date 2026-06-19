package lunar.tinkerer.registry;

import com.mojang.serialization.MapCodec;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import lunar.tinkerer.MagicRevamped;

public class ModRegistryKeys {
    public static final ResourceKey<Registry<Consequence>> CONSEQUENCE = create("consequence");
    public static final ResourceKey<Registry<MapCodec<? extends ConsequenceEffect>>> CONSEQUENCE_EFFECT = create("consequence_effect");

    private static <T> ResourceKey<Registry<T>> create(String path) {
        return ResourceKey.createRegistryKey(MagicRevamped.identifier(path));
    }
}
