package lunar.tinkerer;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipeTypes {
    public static Identifier ID = MagicRevamped.identifier("enchanting");

    public static final RecipeSerializer<EnchantmentRecipe> ENCHANTMENT_RECIPE_SERIALIZER = RecipeSerializer.register(
            ID.toString(),
            new EnchantmentRecipe.Serializer()
    );

    public static final RecipeType<EnchantmentRecipe> ENCHANTMENT_RECIPE_TYPE =  Registry.register(
            Registries.RECIPE_TYPE,
            ID,
            new RecipeType<EnchantmentRecipe>(){
                @Override
                public String toString() {
                    return ID.toString();
                }
            }
    );

    public static final RecipeDisplay.Serializer<EnchantmentRecipeDisplay> ENCHANTMENT_RECIPE_DISPLAY = Registry.register(
            Registries.RECIPE_DISPLAY,
            ID,
            EnchantmentRecipeDisplay.SERIALIZER
    );

    public static final RecipeBookCategory ENCHANTMENT_RECIPE_BOOK_CATEGORY = Registry.register(
            Registries.RECIPE_BOOK_CATEGORY,
            ID,
            new RecipeBookCategory()
    );

    public static void initialize() {}
}
