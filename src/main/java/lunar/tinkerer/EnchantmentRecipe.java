package lunar.tinkerer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

import java.util.List;

public class EnchantmentRecipe implements Recipe<CraftingRecipeInput> {
    public final String group;
    public final ItemStack result;
    public final List<ItemStack> specialIngredients;
    public final List<Ingredient> ingredients;
    @Nullable
    private IngredientPlacement ingredientPlacement;

    public EnchantmentRecipe(String group, ItemStack result, List<Ingredient> ingredients, List<ItemStack> specialIngredients) {
        this.group = group;
        this.result = result;
        this.ingredients = ingredients;
        this.specialIngredients = specialIngredients;
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
        if (craftingRecipeInput.getStackCount() != this.ingredients.size() + this.specialIngredients.size()) {
            return false;
        }
        if(!this.specialIngredients.isEmpty()) {
            return matchesSpecial(craftingRecipeInput);
        }
        if (craftingRecipeInput.size() == 1 && this.ingredients.size() == 1) {
            return this.ingredients.getFirst().test(craftingRecipeInput.getStackInSlot(0));
        }
        return craftingRecipeInput.getRecipeMatcher().isCraftable(this, null);
    }

    public boolean matchesSpecial(CraftingRecipeInput craftingRecipeInput) {
        if (this.specialIngredients.stream().noneMatch(itemStack -> {
            int x = craftingRecipeInput.getStacks().stream().filter(itemStack1 -> itemStack.getComponents().toString().equals(itemStack1.getComponents().toString())).toList().size();
            int y = this.specialIngredients.stream().filter(itemStack1 -> itemStack.getComponents().toString().equals(itemStack1.getComponents().toString())).toList().size();
            return x == y;
        })) {
            return false;
        }
        return craftingRecipeInput.getRecipeMatcher().isCraftable(this, null);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        return this.result.copy();
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new EnchantmentRecipeDisplay(
                this.ingredients.stream().map(Ingredient::toDisplay).toList(),
                this.specialIngredients.stream().map(
                        itemStack -> (SlotDisplay) new SlotDisplay.StackSlotDisplay(itemStack)
                ).toList(),
                new SlotDisplay.StackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(ModBlocks.ENCHANTING_TABLE.asItem())
        ));
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return ModRecipeTypes.ENCHANTMENT_RECIPE_BOOK_CATEGORY;
    }

    public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input) {
        return CraftingRecipe.collectRecipeRemainders(input);
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
                        .forGetter(recipe -> recipe.ingredients),
                ItemStack.VALIDATED_CODEC
                        .listOf(0, 8)
                        .optionalFieldOf("special_ingredients", List.of())
                        .forGetter(recipe -> recipe.specialIngredients)
            ).apply(instance, EnchantmentRecipe::new));
        public static final PacketCodec<RegistryByteBuf, EnchantmentRecipe> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.STRING,
                recipe -> recipe.group,
                ItemStack.PACKET_CODEC,
                recipe -> recipe.result,
                Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()),
                recipe -> recipe.ingredients,
                ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()),
                recipe -> recipe.specialIngredients,
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