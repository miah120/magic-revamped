package lunar.tinkerer.enchantmentRecipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public record EnchantmentRecipeDisplay(List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {
    public static final MapCodec<EnchantmentRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
        SlotDisplay.CODEC.listOf().fieldOf("ingredients").forGetter(EnchantmentRecipeDisplay::ingredients),
        SlotDisplay.CODEC.fieldOf("result").forGetter(EnchantmentRecipeDisplay::result),
        SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(EnchantmentRecipeDisplay::craftingStation)
    ).apply(instance, EnchantmentRecipeDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
        SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()),
        EnchantmentRecipeDisplay::ingredients,
        SlotDisplay.STREAM_CODEC,
        EnchantmentRecipeDisplay::result,
        SlotDisplay.STREAM_CODEC,
        EnchantmentRecipeDisplay::craftingStation,
        EnchantmentRecipeDisplay::new
    );
    public static final RecipeDisplay.Type<EnchantmentRecipeDisplay> SERIALIZER = new RecipeDisplay.Type<>(CODEC, STREAM_CODEC);

    public RecipeDisplay.Type<EnchantmentRecipeDisplay> type() {
        return SERIALIZER;
    }

    public boolean isEnabled(FeatureFlagSet features) {
        return this.ingredients.stream().allMatch((ingredient) -> ingredient.isEnabled(features)) && RecipeDisplay.super.isEnabled(features);
    }
}