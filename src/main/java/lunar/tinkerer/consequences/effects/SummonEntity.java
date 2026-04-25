package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record SummonEntity<T extends Entity>(EntityType<T> entityType, int count) implements ConsequenceEffect {
    @Override
    public ItemStack run(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        Stream<Entity> entities = IntStream.range(0, count)
            .mapToObj(i -> entityType.spawn(world, player.blockPosition(), EntitySpawnReason.MOB_SUMMONED));
        world.addWorldGenChunkEntities(entities);
        return ItemStack.EMPTY;
    }
}
