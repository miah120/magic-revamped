package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.stream.Stream;

public record SummonEntity<T extends Entity>(EntityType<T> entityType) implements ConsequenceEffect {
    @Override
    public ItemStack run(ServerWorld world, BlockPos blockPos, ServerPlayerEntity player, RecipeInputInventory input, ItemStack stack) {
        var entity = entityType.spawn(world, player.getBlockPos(), SpawnReason.MOB_SUMMONED);
        if (entity == null) return ItemStack.EMPTY;
        world.addEntities(Stream.of(entity));
        return ItemStack.EMPTY;
    }
}
