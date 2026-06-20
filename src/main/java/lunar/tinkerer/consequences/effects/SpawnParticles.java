package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;
import net.minecraft.world.phys.Vec3;

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
            FloatProviders.CODEC.optionalFieldOf("speed", ConstantFloat.ZERO).forGetter(SpawnParticles::speed),
            IntProviders.CODEC.optionalFieldOf("count", ConstantInt.ZERO).forGetter(SpawnParticles::count),
            Codec.STRING.optionalFieldOf("target", "decoration").forGetter(SpawnParticles::target)
        )
        .apply(i, SpawnParticles::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
        ServerLevel serverLevel = info.world();
        Entity entity = info.player();
        Vec3 position = switch (this.target) {
            case "player": yield info.player().position();
            case "table": yield info.blockPos().getCenter();
            case "decoration":
            default: yield info.decoration().map(d -> d.getPos().getCenter()).orElse(info.player().position());
        };
        RandomSource random = entity.getRandom();
        Vec3 movement = entity.getKnownMovement();
        float bbWidth = entity.getBbWidth();
        float bbHeight = entity.getBbHeight();
        serverLevel.sendParticles(
            this.particle,
            this.horizontalPosition.getCoordinate(position.x(), position.x(), bbWidth, random),
            this.verticalPosition.getCoordinate(position.y(), position.y() + bbHeight / 2.0F, bbHeight, random),
            this.horizontalPosition.getCoordinate(position.z(), position.z(), bbWidth, random),
            this.count.sample(random),
            this.horizontalVelocity.getVelocity(movement.x(), random),
            this.verticalVelocity.getVelocity(movement.y(), random),
            this.horizontalVelocity.getVelocity(movement.z(), random),
            this.speed.sample(random)
        );
        return ItemStack.EMPTY;
    }
}
