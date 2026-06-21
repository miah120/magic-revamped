package lunar.tinkerer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RuneItem extends Item {
    public static final int DEFAULT_RUNE_FLUX = 8;

    public RuneItem(Properties settings) {
        super(settings);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.get(ModItems.OPEN) != null;
    }

    @Override
    public Component getName(ItemStack stack) {
        Optional<Holder<Enchantment>> optional = Optional.ofNullable(stack.get(ModItems.ENCHANTMENT));
        return optional
            .map(
                enchantment ->
                    Component.translatableWithFallback(
                    this._getTranslationKey(stack),
                "Unknown Rune",
                        Component.translatable(
                            Util.makeDescriptionId(
                                "enchantment",
                                Identifier.parse(enchantment.getRegisteredName())
                            )
                        )
                    )
            )
            .orElse((MutableComponent) super.getName(stack));
    }

    public String _getTranslationKey(ItemStack stack) {
        String base = this.descriptionId + ".template";
        Optional<Unit> open = Optional.ofNullable(stack.get(ModItems.OPEN));
        Optional<Unit> charged = Optional.ofNullable(stack.get(ModItems.CHARGED));
        return charged.isPresent()
                ? base + ".charged"
                : open.isPresent()
                    ? base + ".open"
                    : base;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        var flux = stack.getOrDefault(ModItems.FLUX, DEFAULT_RUNE_FLUX);
        ChatFormatting color;
        if (flux > 6) {
            color = ChatFormatting.DARK_RED;
        } else if (flux > 2) {
            color = ChatFormatting.GRAY;
        } else {
            color = ChatFormatting.DARK_GREEN;
        }
        textConsumer.accept(
            Component.translatable(this.descriptionId + ".flux", flux)
                .withStyle(color)
                .withStyle(ChatFormatting.ITALIC)
        );
    }

    public static void addOpenRunes(CreativeModeTab.Output entries, HolderLookup<Enchantment> registryWrapper, CreativeModeTab.TabVisibility stackVisibility) {
        registryWrapper.listElements().map(enchantmentEntry ->
            {
                ItemStack itemStack = new ItemStack(ModItems.RUNE);
                itemStack.set(ModItems.OPEN, Unit.INSTANCE);
                itemStack.set(ModItems.ENCHANTMENT, enchantmentEntry);
                return itemStack;
            }).forEach(stack -> entries.accept(stack, stackVisibility));
    }

    public static void addOpenAndClosedRunes(CreativeModeTab.Output entries, HolderLookup<Enchantment> registryWrapper, CreativeModeTab.TabVisibility stackVisibility) {
        registryWrapper.listElements().flatMap(enchantmentEntry ->
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
            }).forEach(stack -> entries.accept(stack, stackVisibility));
    }

    public record LeveledEnchantment(Holder<Enchantment> enchantment, int level) {}
    public static Stream<LeveledEnchantment> getEnchantments(ItemStack itemStack) {
        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        return enchantments.entrySet().stream().map(enchantmentRegistryEntryEntry -> {
            var enchantment = enchantmentRegistryEntryEntry.getKey();
            int level = enchantments.getLevel(enchantment);
            return new LeveledEnchantment(enchantment, level);
        });
    }
}
