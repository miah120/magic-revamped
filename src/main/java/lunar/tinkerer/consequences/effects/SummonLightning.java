package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.MapCodec;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.stream.Stream;

public record SummonLightning() implements ConsequenceEffect {
    public static MapCodec<SummonLightning> CODEC = MapCodec.unit(new SummonLightning());

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        int r = 2;
        double r2 = Math.pow(r, 0.5);
        Stream<Entity> bolts = Stream.of(
                new Vec3(0, 0, 0),
                new Vec3(r, 0, 0),
                new Vec3(-r, 0, 0),
                new Vec3(0, 0, r),
                new Vec3(0, 0, -r),
                new Vec3(r2, 0, r2),
                new Vec3(-r2, 0, r2),
                new Vec3(r2, 0, -r2),
                new Vec3(-r2, 0, -r2)
            )
            .map(pos -> pos.add(player.trackingPosition()))
            .map(pos -> {
                var bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
                bolt.setPos(pos);
                return bolt;
            });
        world.addWorldGenChunkEntities(bolts);
        return ItemStack.EMPTY;
    }
}
