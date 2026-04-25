package lunar.tinkerer.consequences;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public interface ConsequenceEffect {
    ItemStack run(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack);
}
