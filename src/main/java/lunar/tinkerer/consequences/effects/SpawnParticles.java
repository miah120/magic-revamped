package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.stream.IntStream;

public record SpawnParticles(
    ParticleOptions particle,
    SpawnParticlesEffect.PositionSource horizontalPosition,
    SpawnParticlesEffect.PositionSource verticalPosition,
    SpawnParticlesEffect.VelocitySource horizontalVelocity,
    SpawnParticlesEffect.VelocitySource verticalVelocity,
    FloatProvider speed,
    IntProvider count,
    String target
) implements ConsequenceEffect {
    public static final MapCodec<SpawnParticles> CODEC = RecordCodecBuilder.mapCodec(
    i -> i.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter(SpawnParticles::particle),
            SpawnParticlesEffect.PositionSource.CODEC.fieldOf("horizontal_position").forGetter(SpawnParticles::horizontalPosition),
            SpawnParticlesEffect.PositionSource.CODEC.fieldOf("vertical_position").forGetter(SpawnParticles::verticalPosition),
            SpawnParticlesEffect.VelocitySource.CODEC.fieldOf("horizontal_velocity").forGetter(SpawnParticles::horizontalVelocity),
            SpawnParticlesEffect.VelocitySource.CODEC.fieldOf("vertical_velocity").forGetter(SpawnParticles::verticalVelocity),
            FloatProviders.CODEC.optionalFieldOf("speed", new ConstantFloat(1f)).forGetter(SpawnParticles::speed),
            IntProviders.CODEC.optionalFieldOf("count", new ConstantInt(1)).forGetter(SpawnParticles::count),
            Codec.STRING.optionalFieldOf("target", "decoration").forGetter(SpawnParticles::target)
        )
        .apply(i, SpawnParticles::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
        Target target = getTarget(info);
        Vec3 position = target.position;
        AABB bounds = target.bounds;
        Vec3 center = bounds.getCenter().add(position);
        RandomSource random = info.player().getRandom();
        IntStream.range(0, this.count.sample(random)).forEach(_ -> info.world().sendParticles(
            this.particle,
            this.horizontalPosition.getCoordinate(center.x(), center.x(), (float) bounds.getXsize(), random),
            this.verticalPosition.getCoordinate(center.y(), center.y(), (float) bounds.getYsize(), random),
            this.horizontalPosition.getCoordinate(center.z(), center.z(), (float) bounds.getZsize(), random),
            0,
            this.horizontalVelocity.getVelocity(0, random),
            this.verticalVelocity.getVelocity(0, random),
            this.horizontalVelocity.getVelocity(0, random),
            this.speed.sample(random)
        ));
        return ItemStack.EMPTY;
    }

    public Target getTarget(Consequence.RunInfo info) {
        if (this.target.equals("player")) return Target.player(info.player());
        Target table = Target.block(new BlockInWorld(info.world(), info.tablePos(), false));
        if (this.target.equals("table")) return table;
        return info.decoration().map(Target::block).orElse(table);
    }

    public record Target(Vec3 position, AABB bounds) {
        static Target player(Player player) {
            return new Target(player.position(), player.getBoundingBox());
        }

        static Target block(BlockInWorld block) {
            return new Target(
                new Vec3(block.getPos()),
                block.getState().getShape(block.getLevel(), block.getPos()).bounds()
            );
        }
    }
}
