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
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentRecipe implements Recipe<CraftingInput> {
    public final String group;
    public final ItemStackTemplate result;
    public final List<Ingredient> ingredients;
    @Nullable
    private PlacementInfo ingredientPlacement;
    private boolean fabric_requiresTesting = false;

    public EnchantmentRecipe(String group, ItemStackTemplate result, List<Ingredient> ingredients) {
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
    public ItemStack assemble(CraftingInput recipeInput) {
        return this.result.create();
    }

    @Override
    public boolean showNotification() {
        return false;
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

    public static final MapCodec<EnchantmentRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
    i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result),
                Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(o -> o.ingredients)
            )
            .apply(i, EnchantmentRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            recipe -> recipe.group,
            ItemStackTemplate.STREAM_CODEC,
            o -> o.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
            o -> o.ingredients,
            EnchantmentRecipe::new
    );
    public static final RecipeSerializer<EnchantmentRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}