package lunar.tinkerer;

import com.google.common.base.Function;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Unit;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.UnaryOperator;

public class ModItems {
    public static ComponentType<Integer> FLUX = registerDataComponent(
            "flux",
            builder -> builder
                    .codec(Codecs.POSITIVE_INT)
                    .packetCodec(PacketCodecs.VAR_INT)
    );

    public static ComponentType<Unit> OPEN = registerDataComponent(
            "open",
            builder -> builder
                    .codec(Unit.CODEC)
                    .packetCodec(Unit.PACKET_CODEC)
    );

    public static ComponentType<RegistryEntry<Enchantment>> ENCHANTMENT = registerDataComponent(
            "enchantment",
            builder -> builder
                    .codec(Enchantment.ENTRY_CODEC)
                    .packetCodec(Enchantment.ENTRY_PACKET_CODEC)
    );

    public static final Item RUNE = register("rune", RuneItem::new, new Item.Settings().component(ENCHANTMENT, null).component(FLUX, RuneItem.DEFAULT_RUNE_FLUX));

    public static void initialize() {
    }

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, MagicRevamped.identifier(name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    private static <T> ComponentType<T> registerDataComponent(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                MagicRevamped.identifier(id),
                builderOperator.apply(ComponentType.builder()).build()
        );
    }


}