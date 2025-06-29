package lunar.tinkerer;

import net.minecraft.recipe.RecipeSerializer;

public class ModRecipeTypes {
    public static final RecipeSerializer<EnchantmentRecipe> ENCHANTMENT_RECIPE = RecipeSerializer.register("enchanting", new EnchantmentRecipe.Serializer());

    public void initialize() {}
}
