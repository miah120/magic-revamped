package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.stream.Stream;

public record SummonLightning() implements ConsequenceEffect {
    @Override
    public ItemStack run(ServerWorld world, BlockPos blockPos, ServerPlayerEntity player, RecipeInputInventory input, ItemStack stack) {
        int r = 2;
        double r2 = Math.pow(r, 0.5);
        Stream<Entity> bolts = Stream.of(
                new Vec3d(0, 0, 0),
                new Vec3d(r, 0, 0),
                new Vec3d(-r, 0, 0),
                new Vec3d(0, 0, r),
                new Vec3d(0, 0, -r),
                new Vec3d(r2, 0, r2),
                new Vec3d(-r2, 0, r2),
                new Vec3d(r2, 0, -r2),
                new Vec3d(-r2, 0, -r2)
        ).map(
            pos -> pos.add(player.getPos())
        ).map(pos -> {
            var bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
            bolt.setPosition(pos);
            return bolt;
        });
        world.addEntities(bolts);
        return ItemStack.EMPTY;
    }
}
