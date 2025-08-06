package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;

public record Teleport(int min, int max) implements ConsequenceEffect {
    @Override
    public ItemStack run(ServerWorld world, BlockPos blockPos, ServerPlayerEntity player, RecipeInputInventory input, ItemStack stack) {
        var target = player.getPos().add(getTarget(world));
        teleportTo(world, player, target.getX(), target.getY(), target.getZ());
        return ItemStack.EMPTY;
    }

    public Vec3d getTarget(ServerWorld world) {
        int size = world.random.nextBetween(min, max);
        double x = world.random.nextDouble();
        double y = world.random.nextDouble();
        double z = world.random.nextDouble();

        return new Vec3d(x, y, z).normalize().multiply(size);
    }

    public static void teleportTo(ServerWorld world, ServerPlayerEntity player, double x, double y, double z) {
        BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);

        while (mutable.getY() > world.getBottomY() && !world.getBlockState(mutable).blocksMovement()) {
            mutable.move(Direction.DOWN);
        }

        BlockState blockState = world.getBlockState(mutable);
        boolean bl = blockState.blocksMovement();
        boolean bl2 = blockState.getFluidState().isIn(FluidTags.WATER);
        if (bl && !bl2) {
            Vec3d vec3d = player.getPos();
            boolean bl3 = player.teleport(x, y, z, true);
            if (bl3) {
                world.emitGameEvent(GameEvent.TELEPORT, vec3d, GameEvent.Emitter.of(player));
                if (!player.isSilent()) {
                    world.playSound(null, player.lastX, player.lastY, player.lastZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, player.getSoundCategory(), 1.0F, 1.0F);
                    player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }
        }
    }

}
