package lunar.tinkerer;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Optional;

public class RuneItem extends Item {

    public RuneItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        Optional<RegistryEntry<Enchantment>> optional = Optional.ofNullable(stack.get(ModItems.ENCHANTMENT));
        return optional
            .map(
                enchantment ->
                    Text.translatable(this.translationKey + ".effect." + getEnchantmentName(enchantment))
            )
            .orElse((MutableText) super.getName(stack));
    }

    public static String getEnchantmentName(RegistryEntry<Enchantment> enchantment) {
        String name = enchantment.getIdAsString();
        return name; //.substring("Enchantment ".length());
    }
}
