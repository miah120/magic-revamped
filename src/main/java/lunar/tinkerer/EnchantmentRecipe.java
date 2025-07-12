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
import java.util.function.Predicate;

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
        //TODO: implement component holder on this instead of... whatever we're doing here
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
        if (
            !containsAllSpecialIngredients(this.specialIngredients, craftingRecipeInput.getStacks())
        ) {
            return false;
        }
        return craftingRecipeInput.getRecipeMatcher().isCraftable(this, null);
    }

    public static boolean containsAllSpecialIngredients(List<ItemStack> requirements, List<ItemStack> present) {
        return requirements.stream().distinct()
            .allMatch(
                itemStack -> countMatching(
                    present,
                    itemStack1 -> containsAllComponentChanges(itemStack, itemStack1)
                ) == countMatching(
                    requirements,
                    itemStack1 -> containsAllComponentChanges(itemStack, itemStack1)
                )
            );
    }

    public static boolean containsAllComponentChanges(ItemStack required, ItemStack present) {
        return required.getComponentChanges().entrySet().stream()
            .allMatch(componentTypeOptionalEntry ->
                componentTypeOptionalEntry.getValue()
                    .map(v -> v == present.get(componentTypeOptionalEntry.getKey()))
                    .orElse(true)
            );
    }

    public static int countMatching(List<ItemStack> stacks, Predicate<? super ItemStack> predicate) {
        return stacks.stream().filter(predicate).toList().size();
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