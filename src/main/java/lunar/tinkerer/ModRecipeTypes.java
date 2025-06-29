package lunar.tinkerer;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipeTypes {
    public static final RecipeSerializer<EnchantmentRecipe> ENCHANTMENT_RECIPE = RecipeSerializer.register(
            Identifier.of(MagicRevamped.MOD_ID, "enchanting").toString(),
            new EnchantmentRecipe.Serializer()
    );

    public static final RecipeDisplay.Serializer<EnchantmentRecipeDisplay> ENCHANTMENT_RECIPE_DISPLAY = Registry.register(
            Registries.RECIPE_DISPLAY,
            Identifier.of(MagicRevamped.MOD_ID, "enchanting"),
            EnchantmentRecipeDisplay.SERIALIZER
    );

    public static void initialize() {}
}
