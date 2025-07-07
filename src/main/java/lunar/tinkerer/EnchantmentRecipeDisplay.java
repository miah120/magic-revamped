package lunar.tinkerer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.resource.featuretoggle.FeatureSet;

import java.util.List;

//TODO: Show tooltip and special ingredients?
public record EnchantmentRecipeDisplay(List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {
    public static final MapCodec<EnchantmentRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            SlotDisplay.CODEC.listOf()
                    .fieldOf("ingredients")
                    .forGetter(EnchantmentRecipeDisplay::ingredients),
            SlotDisplay.CODEC
                    .fieldOf("result")
                    .forGetter(EnchantmentRecipeDisplay::result),
            SlotDisplay.CODEC
                    .fieldOf("crafting_station")
                    .forGetter(EnchantmentRecipeDisplay::craftingStation)
    ).apply(instance, EnchantmentRecipeDisplay::new));
    public static final PacketCodec<RegistryByteBuf, EnchantmentRecipeDisplay> PACKET_CODEC;
    public static final RecipeDisplay.Serializer<EnchantmentRecipeDisplay> SERIALIZER;

    public RecipeDisplay.Serializer<EnchantmentRecipeDisplay> serializer() {
        return SERIALIZER;
    }

    public boolean isEnabled(FeatureSet features) {
        return this.ingredients.stream().allMatch((ingredient) -> ingredient.isEnabled(features)) && RecipeDisplay.super.isEnabled(features);
    }

    static {
        PACKET_CODEC = PacketCodec.tuple(
                SlotDisplay.PACKET_CODEC.collect(PacketCodecs.toList()),
                EnchantmentRecipeDisplay::ingredients,
                SlotDisplay.PACKET_CODEC,
                EnchantmentRecipeDisplay::result,
                SlotDisplay.PACKET_CODEC,
                EnchantmentRecipeDisplay::craftingStation,
                EnchantmentRecipeDisplay::new
        );
        SERIALIZER = new RecipeDisplay.Serializer<>(CODEC, PACKET_CODEC);
    }
}