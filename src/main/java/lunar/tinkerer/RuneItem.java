package lunar.tinkerer;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;

import java.util.Optional;
import java.util.stream.Stream;

public class RuneItem extends Item {

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

    public record LeveledEnchantment(RegistryEntry<Enchantment> enchantment, int level) {}
    public static Stream<LeveledEnchantment> getEnchantments(ItemStack itemStack) {
        var enchantments = itemStack.getEnchantments();
        return enchantments.getEnchantmentEntries().stream().map(enchantmentRegistryEntryEntry -> {
            var enchantment = enchantmentRegistryEntryEntry.getKey();
            int level = enchantments.getLevel(enchantment);
            return new LeveledEnchantment(enchantment, level);
        });
    }
}
