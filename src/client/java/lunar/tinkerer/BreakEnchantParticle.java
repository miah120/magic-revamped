package lunar.tinkerer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class BreakEnchantParticle extends SimpleAnimatedParticle {
    double initialVelocityX;
    double initialVelocityY;
    double initialVelocityZ;

    BreakEnchantParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, spriteProvider, 0F);
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
        this.initialVelocityX = velocityX;
        this.initialVelocityY = velocityY;
        this.initialVelocityZ = velocityZ;
        this.quadSize *= 0.75F;
        this.lifetime = 50 + this.random.nextInt(22);
        this.setSpriteFromAge(spriteProvider);
    }

    public static double particleSpeed(double velocity, double initialVelocity) {
        return velocity - initialVelocity / 65;
    }

    @Override
    public void tick() {
        this.xd = particleSpeed(this.xd, this.initialVelocityX);
        this.yd = particleSpeed(this.yd, this.initialVelocityY);
        this.zd = particleSpeed(this.zd, this.initialVelocityZ);
        super.tick();
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType simpleParticleType, @NonNull ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, @NonNull RandomSource random
        ) {
            return new BreakEnchantParticle(clientLevel, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}
