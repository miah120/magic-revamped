package lunar.tinkerer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public class ModRecipeTypes {
    public static Identifier ID = MagicRevamped.identifier("enchanting");

    public static final RecipeSerializer<EnchantmentRecipe> ENCHANTMENT_RECIPE_SERIALIZER = RecipeSerializer.register(
            ID.toString(),
            new EnchantmentRecipe.Serializer()
    );

    public static final RecipeType<EnchantmentRecipe> ENCHANTMENT_RECIPE_TYPE =  Registry.register(
            BuiltInRegistries.RECIPE_TYPE,
            ID,
            new RecipeType<EnchantmentRecipe>(){
                @Override
                public String toString() {
                    return ID.toString();
                }
            }
    );

    public static final RecipeDisplay.Type<EnchantmentRecipeDisplay> ENCHANTMENT_RECIPE_DISPLAY = Registry.register(
            BuiltInRegistries.RECIPE_DISPLAY,
            ID,
            EnchantmentRecipeDisplay.SERIALIZER
    );

    public static final RecipeBookCategory ENCHANTMENT_RECIPE_BOOK_CATEGORY = Registry.register(
            BuiltInRegistries.RECIPE_BOOK_CATEGORY,
            ID,
            new RecipeBookCategory()
    );

    public static void initialize() {}
}
