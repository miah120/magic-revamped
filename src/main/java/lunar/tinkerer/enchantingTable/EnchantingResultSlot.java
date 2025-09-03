package lunar.tinkerer.enchantingTable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;

import static lunar.tinkerer.enchantingTable.ModEnchantmentScreenHandler.getLevelRequirement;

public class EnchantingResultSlot extends CraftingResultSlot {
    public ModEnchantmentScreenHandler handler;
    public RecipeInputInventory input;

    public EnchantingResultSlot(ModEnchantmentScreenHandler handler, PlayerEntity player, RecipeInputInventory input, Inventory inventory, int index, int x, int y) {
        super(player, input, inventory, index, x, y);
        this.input = input;
        this.handler = handler;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        if (this.handler.timeout.get() > 0) return false;
        int levelRequirement = getLevelRequirement(this.input, this.handler.player.getWorld());
        return playerEntity.experienceLevel >= levelRequirement;
    }

    @Override
    public void onCrafted(ItemStack stack) {
        super.onCrafted(stack);
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.handler.onTakeResult(player, stack);
    }
}
