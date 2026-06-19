package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record Teleport(int min, int max) implements ConsequenceEffect {
    @Override
    public ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        var target = player.trackingPosition().add(getTarget(world));
        teleportTo(world, player, target.x(), target.y(), target.z());
        return ItemStack.EMPTY;
    }

    public Vec3 getTarget(ServerLevel world) {
        int size = world.getRandom().nextIntBetweenInclusive(min, max);
        double x = world.getRandom().nextDouble();
        double y = world.getRandom().nextDouble();
        double z = world.getRandom().nextDouble();

        return new Vec3(x, y, z).normalize().scale(size);
    }

    public static void teleportTo(ServerLevel world, ServerPlayer player, double x, double y, double z) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, y, z);

        while (mutable.getY() > world.getMinY() && !world.getBlockState(mutable).blocksMotion()) {
            mutable.move(Direction.DOWN);
        }

        BlockState blockState = world.getBlockState(mutable);
        boolean bl = blockState.blocksMotion();
        boolean bl2 = blockState.getFluidState().is(FluidTags.WATER);
        if (bl && !bl2) {
            Vec3 vec3d = player.trackingPosition();
            boolean bl3 = player.randomTeleport(x, y, z, true);
            if (bl3) {
                world.gameEvent(GameEvent.TELEPORT, vec3d, GameEvent.Context.of(player));
                if (!player.isSilent()) {
                    world.playSound(null, player.xo, player.yo, player.zo, SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 1.0F);
                    player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }
        }
    }

}
