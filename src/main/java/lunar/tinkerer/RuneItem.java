package lunar.tinkerer;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RuneItem extends Item {
    public static final int DEFAULT_RUNE_FLUX = 8;

    public RuneItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.get(ModItems.OPEN) != null;
    }

    @Override
    public Text getName(ItemStack stack) {
        Optional<RegistryEntry<Enchantment>> optional = Optional.ofNullable(stack.get(ModItems.ENCHANTMENT));
        return optional
            .map(
                enchantment ->
                    Text.translatableWithFallback(
                    this._getTranslationKey(stack),
                "Unknown Rune",
                        Text.translatable(
                            Util.createTranslationKey(
                                "enchantment",
                                Identifier.of(enchantment.getIdAsString())
                            )
                        )
                    )
            )
            .orElse((MutableText) super.getName(stack));
    }

    public String _getTranslationKey(ItemStack stack) {
        String base = this.translationKey + ".template";
        Optional<Unit> optional = Optional.ofNullable(stack.get(ModItems.OPEN));
        return optional.isEmpty()
                ? base
                : base + ".open";
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        var flux = stack.getOrDefault(ModItems.FLUX, DEFAULT_RUNE_FLUX);
        Formatting color;
        if (flux > 6) {
            color = Formatting.DARK_RED;
        } else if (flux > 2) {
            color = Formatting.GRAY;
        } else {
            color = Formatting.DARK_GREEN;
        }
        textConsumer.accept(
            Text.translatable(this.translationKey + ".flux", flux)
                .formatted(color)
                .formatted(Formatting.ITALIC)
        );
    }

    public static void addOpenRunes(ItemGroup.Entries entries, RegistryWrapper<Enchantment> registryWrapper, ItemGroup.StackVisibility stackVisibility) {
        registryWrapper.streamEntries().map(enchantmentEntry ->
            {
                ItemStack itemStack = new ItemStack(ModItems.RUNE);
                itemStack.set(ModItems.OPEN, Unit.INSTANCE);
                itemStack.set(ModItems.ENCHANTMENT, enchantmentEntry);
                return itemStack;
            }).forEach(stack -> entries.add(stack, stackVisibility));
    }

    public static void addOpenAndClosedRunes(ItemGroup.Entries entries, RegistryWrapper<Enchantment> registryWrapper, ItemGroup.StackVisibility stackVisibility) {
        registryWrapper.streamEntries().flatMap(enchantmentEntry ->
            {
                ItemStack closed = new ItemStack(ModItems.RUNE);
                closed.set(ModItems.ENCHANTMENT, enchantmentEntry);

                ItemStack open = new ItemStack(ModItems.RUNE);
                open.set(ModItems.ENCHANTMENT, enchantmentEntry);
                open.set(ModItems.OPEN, Unit.INSTANCE);

                return Stream.of(
                    closed,
                    open
                );
            }).forEach(stack -> entries.add(stack, stackVisibility));
    }

    public record LeveledEnchantment(RegistryEntry<Enchantment> enchantment, int level) {}
    public static Stream<LeveledEnchantment> getEnchantments(ItemStack itemStack) {
        var enchantments = EnchantmentHelper.getEnchantments(itemStack);
        return enchantments.getEnchantmentEntries().stream().map(enchantmentRegistryEntryEntry -> {
            var enchantment = enchantmentRegistryEntryEntry.getKey();
            int level = enchantments.getLevel(enchantment);
            return new LeveledEnchantment(enchantment, level);
        });
    }
}
