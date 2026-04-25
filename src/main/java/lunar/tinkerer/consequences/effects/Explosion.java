package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record Explosion() implements ConsequenceEffect {
    @Override
    public ItemStack run(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        world.explode(null, world.damageSources().magic(), null, blockPos.getCenter().add(0, 1, 0), 10.0F, true, Level.ExplosionInteraction.BLOCK);
        return ItemStack.EMPTY;
    }
}
