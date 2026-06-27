package lunar.tinkerer;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class RuneItem extends Item {
    public static final Flux DEFAULT_RUNE_FLUX = new Flux(8);

    public RuneItem(Properties settings) {
        super(settings);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.get(MagicRevamped.DataComponents.OPEN) != null;
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        return RuneItem.getEnchantment(stack)
            .map(Holder::getRegisteredName)
            .map(Identifier::parse)
            .map(enchantment -> Util.makeDescriptionId("enchantment", enchantment))
            .map(Component::translatable)
            .map(enchantment -> Component.translatableWithFallback(
                this.getTranslationKey(stack), "Unknown Rune", enchantment
            ))
            .orElse((MutableComponent) super.getName(stack));
    }

    public String getTranslationKey(ItemStack stack) {
        String base = this.descriptionId + ".template";
        Optional<Unit> charged = Optional.ofNullable(stack.get(MagicRevamped.DataComponents.CHARGED));
        Optional<Unit> open = Optional.ofNullable(stack.get(MagicRevamped.DataComponents.OPEN));
        return charged.isPresent() ? base + ".charged"
            : open.isPresent() ? base + ".open"
            : base;
    }

    public static ItemStack makeRune(Holder<Enchantment> enchantmentEntry, boolean open) {
        return makeRune(enchantmentEntry, open, false, 1, DEFAULT_RUNE_FLUX);
    }

    public static ItemStack makeRune(Holder<Enchantment> enchantmentEntry, boolean open, boolean charged, int count, Flux flux) {
        ItemStack itemStack = new ItemStack(MagicRevamped.Items.RUNE, count);
        itemStack.set(MagicRevamped.DataComponents.ENCHANTMENT, enchantmentEntry);
        itemStack.set(MagicRevamped.DataComponents.FLUX, flux);
        if (open) { itemStack.set(MagicRevamped.DataComponents.OPEN, Unit.INSTANCE); }
        if (charged) { itemStack.set(MagicRevamped.DataComponents.CHARGED, Unit.INSTANCE); }
        return itemStack;
    }

    public static void addOpenRunes(CreativeModeTab.Output entries, HolderLookup<Enchantment> registryWrapper, CreativeModeTab.TabVisibility stackVisibility) {
        registryWrapper.listElements()
            .map(enchantmentEntry -> makeRune(enchantmentEntry, true))
            .forEach(stack -> entries.accept(stack, stackVisibility));
    }

    public static void addOpenAndClosedRunes(CreativeModeTab.Output entries, HolderLookup<Enchantment> registryWrapper, CreativeModeTab.TabVisibility stackVisibility) {
        registryWrapper.listElements()
            .flatMap(enchantmentEntry ->
                Stream.of(makeRune(enchantmentEntry, false), makeRune(enchantmentEntry, true))
            )
            .forEach(stack -> entries.accept(stack, stackVisibility));
    }

    public record LeveledEnchantment(Holder<Enchantment> enchantment, int level) {
        public int cost() {
            return this.level * this.enchantment.value().getAnvilCost();
        }
    }

    public static Stream<LeveledEnchantment> getEnchantments(ItemStack itemStack) {
        return EnchantmentHelper.getEnchantmentsForCrafting(itemStack)
            .entrySet().stream()
            .map(e -> new LeveledEnchantment(e.getKey(), e.getIntValue()));
    }

    public static Optional<Holder<Enchantment>> getEnchantment(ItemStack rune) {
        return Optional.ofNullable(rune.get(MagicRevamped.DataComponents.ENCHANTMENT));
    }

    public static Optional<Flux> changeFlux(ItemStack itemStack, Function<Integer, Integer> transform) {
        int current = itemStack.getOrDefault(MagicRevamped.DataComponents.FLUX, DEFAULT_RUNE_FLUX).value();
        int next = Math.max(0, transform.apply(current));
        return Optional.of(new Flux(next)).filter(_ -> current != next);
    }

    public static int order(ItemStack itemStack) {
        return itemStack.get(MagicRevamped.DataComponents.CHARGED) != null ? 10
            : itemStack.get(MagicRevamped.DataComponents.OPEN) != null ? 1
            : 0;
    }

    public record Flux(Integer value) implements TooltipProvider {
        public static final Codec<Flux> CODEC = Codec.INT.xmap(Flux::new, Flux::value);
        public static final StreamCodec<RegistryFriendlyByteBuf, Flux> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, Flux::value, Flux::new);

        @Override
        public void addToTooltip(@NonNull TooltipContext context, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
            var flux = components.getOrDefault(MagicRevamped.DataComponents.FLUX, DEFAULT_RUNE_FLUX).value();
            ChatFormatting color = getColor(flux);
            consumer.accept(Component.translatable("item.flux", flux).withStyle(color, ChatFormatting.ITALIC));
        }

        public static ChatFormatting getColor(int flux) {
            if (flux > 6) {
                return ChatFormatting.DARK_RED;
            } else if (flux > 2) {
                return ChatFormatting.GRAY;
            } else {
                return ChatFormatting.DARK_GREEN;
            }
        }
    }
}
