package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.MapCodec;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

public record SummonLightning() implements ConsequenceEffect {
    public static MapCodec<SummonLightning> CODEC = MapCodec.unit(new SummonLightning());

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
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
            .map(pos -> pos.add(info.player().trackingPosition()))
            .map(pos -> {
                var bolt = new LightningBolt(EntityTypes.LIGHTNING_BOLT, info.world());
                bolt.setPos(pos);
                return bolt;
            });
        info.world().addWorldGenChunkEntities(bolts);
        return ItemStack.EMPTY;
    }
}
