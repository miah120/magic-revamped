package lunar.tinkerer.enchantingTable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public class EnchantingTableResultInventory implements Container,
        RecipeCraftingHolder {
    private final NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
    @Nullable
    private RecipeHolder<?> lastRecipe;

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.stacks) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.stacks.getFirst();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.takeItem(this.stacks, 0);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.stacks, 0);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.stacks.set(0, stack);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.stacks.clear();
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
        this.lastRecipe = recipe;
    }

    @Override
    @Nullable
    public RecipeHolder<?> getRecipeUsed() {
        return this.lastRecipe;
    }
}