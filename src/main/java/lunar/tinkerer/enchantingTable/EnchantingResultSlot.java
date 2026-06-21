package lunar.tinkerer.enchantingTable;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

import static lunar.tinkerer.enchantingTable.ModEnchantmentScreenHandler.getLevelRequirement;

public class EnchantingResultSlot extends ResultSlot {
    public ModEnchantmentScreenHandler handler;
    public CraftingContainer input;

    public EnchantingResultSlot(ModEnchantmentScreenHandler handler, Player player, CraftingContainer input, Container inventory, int index, int x, int y) {
        super(player, input, inventory, index, x, y);
        this.input = input;
        this.handler = handler;
    }

    @Override
    public boolean mayPickup(Player playerEntity) {
        if (this.handler.timeout.get() > 0) return false;
        int levelRequirement = getLevelRequirement(this.input, this.handler.player.level(), this.handler.getBlockPos());
        return playerEntity.experienceLevel >= levelRequirement;
    }

    @Override
    public void checkTakeAchievements(ItemStack stack) {
        super.checkTakeAchievements(stack);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        this.handler.onTakeResult(player, stack);
    }
}
