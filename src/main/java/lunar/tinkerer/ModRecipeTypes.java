package lunar.tinkerer;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipeTypes {
    public static final RecipeSerializer<EnchantmentRecipe> ENCHANTMENT_RECIPE_SERIALIZER = RecipeSerializer.register(
            Identifier.of(MagicRevamped.MOD_ID, "enchanting").toString(),
            new EnchantmentRecipe.Serializer()
    );

    public static final RecipeType<EnchantmentRecipe> ENCHANTMENT_RECIPE_TYPE =  Registry.register(
            Registries.RECIPE_TYPE,
            Identifier.of(MagicRevamped.MOD_ID, "enchanting"),
            new RecipeType<EnchantmentRecipe>(){
                @Override
                public String toString() {
                    return Identifier.of(MagicRevamped.MOD_ID, "enchanting").toString();
                }
            }
    );


    public static final RecipeDisplay.Serializer<EnchantmentRecipeDisplay> ENCHANTMENT_RECIPE_DISPLAY = Registry.register(
            Registries.RECIPE_DISPLAY,
            Identifier.of(MagicRevamped.MOD_ID, "enchanting"),
            EnchantmentRecipeDisplay.SERIALIZER
    );

    public static void initialize() {}
}
