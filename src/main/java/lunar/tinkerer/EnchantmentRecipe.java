package lunar.tinkerer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.ShapelessMatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnchantmentRecipe implements Recipe<CraftingInput> {
    public final String group;
    public final ItemStackTemplate result;
    public final ShapedRecipePattern pattern;
    public final List<Ingredient> ingredients;
    private final boolean fabric_requiresTesting;

    public EnchantmentRecipe(String group, ItemStackTemplate result, ShapedRecipePattern pattern) {
        this.group = group;
        this.result = result;
        this.pattern = pattern;
        this.ingredients = this.pattern.ingredients().stream().flatMap(Optional::stream).toList();
        this.fabric_requiresTesting = pattern.ingredients().stream().flatMap(Optional::stream).anyMatch(FabricIngredient::requiresTesting);
    }

    @Override
    public @NonNull RecipeType<EnchantmentRecipe> getType() {
        return ModRecipeTypes.ENCHANTMENT_RECIPE_TYPE;
    }

    @Override
    public @NonNull RecipeSerializer<EnchantmentRecipe> getSerializer() {
        return ModRecipeTypes.ENCHANTMENT_RECIPE_SERIALIZER;
    }

    @Override
    public @NonNull String group() {
        return this.group;
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        return PlacementInfo.createFromOptionals(this.pattern.ingredients());
    }


    @Override
    public boolean matches(CraftingInput craftingRecipeInput, @NonNull Level world) {
        if (fabric_requiresTesting) {
            List<ItemStack> nonEmptyStacks = new ArrayList<>(craftingRecipeInput.ingredientCount());

            for (int i = 0; i < craftingRecipeInput.size(); ++i) {
                ItemStack stack = craftingRecipeInput.getItem(i);

                if (!stack.isEmpty()) {
                    nonEmptyStacks.add(stack);
                }
            }

            return ShapelessMatch.isMatch(nonEmptyStacks, this.ingredients);
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
    public @NonNull ItemStack assemble(CraftingInput recipeInput) {
        return this.result.create();
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public @NonNull List<RecipeDisplay> display() {
        return List.of(
            new ShapedCraftingRecipeDisplay(
                this.pattern.width(),
                this.pattern.height(),
                this.pattern.ingredients().stream().map(e -> e.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList(),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Blocks.ENCHANTING_TABLE.asItem())
            )
        );
    }

    @Override
    public @NonNull RecipeBookCategory recipeBookCategory() {
        return ModRecipeTypes.ENCHANTMENT_RECIPE_BOOK_CATEGORY;
    }

    public static final MapCodec<EnchantmentRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
    i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result),
                ShapedRecipePattern.MAP_CODEC.forGetter(o -> o.pattern)
            )
            .apply(i, EnchantmentRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            recipe -> recipe.group,
            ItemStackTemplate.STREAM_CODEC,
            o -> o.result,
            ShapedRecipePattern.STREAM_CODEC,
            o -> o.pattern,
            EnchantmentRecipe::new
    );
    public static final RecipeSerializer<EnchantmentRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}