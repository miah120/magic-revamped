package lunar.tinkerer;

import com.google.common.base.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import java.util.function.UnaryOperator;

public class ModItems {
    public static DataComponentType<Integer> FLUX = registerDataComponent(
            "flux",
            builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
    );

    public static DataComponentType<Unit> OPEN = registerDataComponent(
            "open",
            builder -> builder
                    .persistent(Unit.CODEC)
                    .networkSynchronized(Unit.STREAM_CODEC)
    );

    public static DataComponentType<Unit> CHARGED = registerDataComponent(
            "charged",
            builder -> builder
                    .persistent(Unit.CODEC)
                    .networkSynchronized(Unit.STREAM_CODEC)
    );

    public static DataComponentType<Holder<Enchantment>> ENCHANTMENT = registerDataComponent(
            "enchantment",
            builder -> builder
                    .persistent(Enchantment.CODEC)
                    .networkSynchronized(Enchantment.STREAM_CODEC)
    );

    public static final Item RUNE = register(
        "rune",
        RuneItem::new,
        new Item.Properties()
            .component(ENCHANTMENT, null)
            .component(FLUX, RuneItem.DEFAULT_RUNE_FLUX)
    );

    public static final TagKey<Item> DROPS_CHARGED_RUNE = TagKey.create(Registries.ITEM, MagicRevamped.identifier("drops_charged_rune"));
    public static final TagKey<Item> SUMMONS_LIGHTNING = TagKey.create(Registries.ITEM, MagicRevamped.identifier("summons_lightning"));

    public static void initialize() {
        addBlocksToItemGroup();
    }

    public static void addBlocksToItemGroup() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(content ->
            content.getContext().holders().lookup(Registries.ENCHANTMENT).ifPresent(registryWrapper -> {
                RuneItem.addOpenRunes(content, registryWrapper, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                RuneItem.addOpenAndClosedRunes(content, registryWrapper, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
            })
        );
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
            content.addAfter(Items.ENCHANTING_TABLE, ModBlocks.ENCHANTING_TABLE);
        });
    }

    public static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        // Create the item key.
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, MagicRevamped.identifier(name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.setId(itemKey));

        // Register the item.
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }

    private static <T> DataComponentType<T> registerDataComponent(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                MagicRevamped.identifier(id),
                builderOperator.apply(DataComponentType.builder()).build()
        );
    }


}