package lunar.tinkerer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.impl.recipe.ingredient.ShapelessMatch;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EnchantmentRecipe implements Recipe<CraftingRecipeInput> {
    public final String group;
    public final ItemStack result;
    public final List<Ingredient> ingredients;
    @Nullable
    private IngredientPlacement ingredientPlacement;
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
    public String getGroup() {
        return this.group;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = IngredientPlacement.forShapeless(this.ingredients);
        }
        return this.ingredientPlacement;
    }

    @Override
    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        if (fabric_requiresTesting) {
            List<ItemStack> nonEmptyStacks = new ArrayList<>(craftingRecipeInput.getStackCount());

            for (int i = 0; i < craftingRecipeInput.size(); ++i) {
                ItemStack stack = craftingRecipeInput.getStackInSlot(i);

                if (!stack.isEmpty()) {
                    nonEmptyStacks.add(stack);
                }
            }

            return ShapelessMatch.isMatch(nonEmptyStacks, ingredients);
        }
        if (craftingRecipeInput.getStackCount() != this.ingredients.size()) {
            return false;
        } else {
            return craftingRecipeInput.size() == 1 && this.ingredients.size() == 1
                ? this.ingredients.getFirst().test(craftingRecipeInput.getStackInSlot(0))
                : craftingRecipeInput.getRecipeMatcher().isCraftable(this, null);
        }
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        return this.result.copy();
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new EnchantmentRecipeDisplay(
                this.ingredients.stream().map(Ingredient::toDisplay).toList(),
                new SlotDisplay.StackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(ModBlocks.ENCHANTING_TABLE.asItem())
        ));
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return ModRecipeTypes.ENCHANTMENT_RECIPE_BOOK_CATEGORY;
    }

    public static class Serializer
            implements RecipeSerializer<EnchantmentRecipe> {
        private static final MapCodec<EnchantmentRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING
                        .optionalFieldOf("group", "")
                        .forGetter(recipe -> recipe.group),
                ItemStack.VALIDATED_CODEC
                        .fieldOf("result")
                        .forGetter(recipe -> recipe.result),
                Ingredient.CODEC
                        .listOf(1, 9)
                        .fieldOf("ingredients")
                        .forGetter(recipe -> recipe.ingredients)
            ).apply(instance, EnchantmentRecipe::new));
        public static final PacketCodec<RegistryByteBuf, EnchantmentRecipe> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.STRING,
                recipe -> recipe.group,
                ItemStack.PACKET_CODEC,
                recipe -> recipe.result,
                Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()),
                recipe -> recipe.ingredients,
                EnchantmentRecipe::new
        );

        @Override
        public MapCodec<EnchantmentRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, EnchantmentRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}