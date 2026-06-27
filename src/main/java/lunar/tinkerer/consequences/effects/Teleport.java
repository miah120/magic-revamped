package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

public record Teleport(int min, int max) implements ConsequenceEffect {
    public static MapCodec<Teleport> CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
            Codec.INT.optionalFieldOf("min_range", 25).forGetter(Teleport::min),
            Codec.INT.optionalFieldOf("max_range", 50).forGetter(Teleport::max)
        ).apply(i, Teleport::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
        var target = info.player().blockPosition().offset(getTarget(info.world()));
        teleportTo(info.world(), info.player(), target);
        return ItemStack.EMPTY;
    }

    public BlockPos getTarget(ServerLevel world) {
        var r = world.getRandom();
        Vec3 target = new Vec3(r.nextDouble(), 0, r.nextDouble())
            .normalize()
            .scale(r.nextIntBetweenInclusive(min, max));
        return new BlockPos((int) target.x, max, (int) target.z);
    }

    public static void teleportTo(ServerLevel level, ServerPlayer player, BlockPos blockPos) {
        Vec3 oldPos = player.position();
        Stream.iterate(
                blockPos,
                curPos -> curPos.getY() > level.getMinY(),
                BlockPos::below
            )
            .map(pos -> new BlockInWorld(level, pos, true))
            .filter(block -> block.getState().isFaceSturdy(level, block.getPos(), Direction.UP))
            .filter(block -> !block.getState().getFluidState().is(FluidTags.WATER))
            .findFirst()
            .map(BlockInWorld::getPos)
            .filter(pos -> player.randomTeleport(pos.getX(), pos.getY() + 1, pos.getZ(), true))
            .ifPresent(_ -> {
                level.gameEvent(GameEvent.TELEPORT, oldPos, GameEvent.Context.of(player));
                if (player.isSilent()) return;
                level.playSound(null, player.xo, player.yo, player.zo, SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 1.0F);
                player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            });
    }

}
