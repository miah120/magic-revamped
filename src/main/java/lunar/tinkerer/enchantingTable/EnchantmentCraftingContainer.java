package lunar.tinkerer.enchantingTable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.stream.Stream;

public class EnchantmentCraftingContainer implements CraftingContainer {
    private final AbstractContainerMenu handler;
    private final NonNullList<ItemStack> stacks = NonNullList.withSize(9, ItemStack.EMPTY);

    public EnchantmentCraftingContainer(ModEnchantmentScreenHandler handler) {
        this.handler = handler;
    }

    @Override
    public void fillStackedContents(@NonNull StackedItemContents finder) {
        this.stacks.forEach(finder::accountSimpleStack);
    }

    @Override
    public void clearContent() {
        this.stacks.clear();
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        return this.stacks.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        if (slot >= this.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return this.stacks.get(slot);
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.stacks, slot);
    }


    @Override
    public @NonNull ItemStack removeItem(int slot, int amount) {
        ItemStack itemStack = ContainerHelper.removeItem(this.stacks, slot, amount);
        if (!itemStack.isEmpty()) {
            this.handler.slotsChanged(this);
        }
        return itemStack;
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack stack) {
        this.stacks.set(slot, stack);
        this.handler.slotsChanged(this);
    }

    @Override
    public void setChanged() {
        this.handler.slotsChanged(this);
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public @NonNull List<ItemStack> getItems() {
        return List.copyOf(this.stacks);
    }

    public ItemStack getConduit() { return this.getItem(0); }

    public Stream<ItemStack> getNonEmptyAdditions() { return this.stacks.subList(1, 9).stream().filter(i -> !i.isEmpty()); }


}


