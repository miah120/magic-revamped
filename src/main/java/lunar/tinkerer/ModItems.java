package lunar.tinkerer;

import com.google.common.base.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.UnaryOperator;

public class ModItems {
    public static ComponentType<Integer> FLUX = registerDataComponent(
            "flux",
            builder -> builder
                    .codec(Codecs.NON_NEGATIVE_INT)
                    .packetCodec(PacketCodecs.VAR_INT)
    );

    public static ComponentType<Unit> OPEN = registerDataComponent(
            "open",
            builder -> builder
                    .codec(Unit.CODEC)
                    .packetCodec(Unit.PACKET_CODEC)
    );

    public static ComponentType<Unit> CHARGED = registerDataComponent(
            "charged",
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

    public static final Item RUNE = register(
        "rune",
        RuneItem::new,
        new Item.Settings()
            .component(ENCHANTMENT, null)
            .component(FLUX, RuneItem.DEFAULT_RUNE_FLUX)
    );

    public static final TagKey<Item> DROPS_CHARGED_RUNE = TagKey.of(RegistryKeys.ITEM, MagicRevamped.identifier("drops_charged_rune"));
    public static final TagKey<Item> SUMMONS_LIGHTNING = TagKey.of(RegistryKeys.ITEM, MagicRevamped.identifier("summons_lightning"));

    public static void initialize() {
        addBlocksToItemGroup();
    }

    public static void addBlocksToItemGroup() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content ->
            content.getContext().lookup().getOptional(RegistryKeys.ENCHANTMENT).ifPresent(registryWrapper -> {
                RuneItem.addOpenRunes(content, registryWrapper, ItemGroup.StackVisibility.PARENT_TAB_ONLY);
                RuneItem.addOpenAndClosedRunes(content, registryWrapper, ItemGroup.StackVisibility.SEARCH_TAB_ONLY);
            })
        );
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.addAfter(Items.ENCHANTING_TABLE, ModBlocks.ENCHANTING_TABLE);
        });
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