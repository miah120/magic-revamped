package lunar.tinkerer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.impl.recipe.ingredient.ShapelessMatch;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentRecipe implements Recipe<CraftingInput> {
    public final String group;
    public final ItemStack result;
    public final List<Ingredient> ingredients;
    @Nullable
    private PlacementInfo ingredientPlacement;
    private boolean fabric_requiresTesting = false;

    public EnchantmentRecipe(String group, ItemStack result, List<Ingredient> ingredients) {
        this.group = group;
        this.result = result;
        this.ingredients = ingredients;
        for (Ingredient ingredient : ingredients) {
            if (ingredient.requiresTesting()) {
                fabric_requiresTesting = true;
                break;
            }
        }
    }

    @Override
    public RecipeType<EnchantmentRecipe> getType() {
        return ModRecipeTypes.ENCHANTMENT_RECIPE_TYPE;
    }

    @Override
    public RecipeSerializer<EnchantmentRecipe> getSerializer() {
        return ModRecipeTypes.ENCHANTMENT_RECIPE_SERIALIZER;
    }

    @Override
    public String group() {
        return this.group;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = PlacementInfo.create(this.ingredients);
        }
        return this.ingredientPlacement;
    }

    @Override
    public boolean matches(CraftingInput craftingRecipeInput, Level world) {
        if (fabric_requiresTesting) {
            List<ItemStack> nonEmptyStacks = new ArrayList<>(craftingRecipeInput.ingredientCount());

            for (int i = 0; i < craftingRecipeInput.size(); ++i) {
                ItemStack stack = craftingRecipeInput.getItem(i);

                if (!stack.isEmpty()) {
                    nonEmptyStacks.add(stack);
                }
            }

            return ShapelessMatch.isMatch(nonEmptyStacks, ingredients);
        }
        if (craftingRecipeInput.ingredientCount() != this.ingredients.size()) {
            return false;
        } else {
            return craftingRecipeInput.size() == 1 && this.ingredients.size() == 1
                ? this.ingredients.getFirst().test(craftingRecipeInput.getItem(0))
                : craftingRecipeInput.stackedContents().canCraft(this, null);
        }
    }

    @Override
    public ItemStack assemble(CraftingInput recipeInput, HolderLookup.Provider provider) {
        return this.result.copy();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new EnchantmentRecipeDisplay(
                this.ingredients.stream().map(Ingredient::display).toList(),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(ModBlocks.ENCHANTING_TABLE.asItem())
        ));
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeTypes.ENCHANTMENT_RECIPE_BOOK_CATEGORY;
    }

    public static class Serializer
            implements RecipeSerializer<EnchantmentRecipe> {
        private static final MapCodec<EnchantmentRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING
                        .optionalFieldOf("group", "")
                        .forGetter(recipe -> recipe.group),
                ItemStack.STRICT_CODEC
                        .fieldOf("result")
                        .forGetter(recipe -> recipe.result),
                Ingredient.CODEC
                        .listOf(1, 9)
                        .fieldOf("ingredients")
                        .forGetter(recipe -> recipe.ingredients)
            ).apply(instance, EnchantmentRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentRecipe> PACKET_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                recipe -> recipe.group,
                ItemStack.STREAM_CODEC,
                recipe -> recipe.result,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
                recipe -> recipe.ingredients,
                EnchantmentRecipe::new
        );

        @Override
        public MapCodec<EnchantmentRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EnchantmentRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}