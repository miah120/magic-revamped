package lunar.tinkerer.enchantingTable;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import static lunar.tinkerer.enchantingTable.ModEnchantmentScreenHandler.getLevelRequirement;

public class EnchantingResultSlot extends ResultSlot {
    public ModEnchantmentScreenHandler handler;
    public EnchantmentCraftingContainer input;

    public EnchantingResultSlot(ModEnchantmentScreenHandler handler, Player player, EnchantmentCraftingContainer input, Container inventory, int index, int x, int y) {
        super(player, input, inventory, index, x, y);
        this.input = input;
        this.handler = handler;
    }

    @Override
    public boolean mayPickup(@NonNull Player playerEntity) {
        if (this.handler.timeout.get() > 0) return false;
        int levelRequirement = getLevelRequirement(this.input, this.handler.player.level(), this.handler.getBlockPos());
        return playerEntity.experienceLevel >= levelRequirement;
    }

    @Override
    public void checkTakeAchievements(@NonNull ItemStack stack) {
        super.checkTakeAchievements(stack);
    }

    @Override
    public void onTake(@NonNull Player player, @NonNull ItemStack stack) {
        this.handler.onTakeResult(player, stack);
    }
}
