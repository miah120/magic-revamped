package lunar.tinkerer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class BreakEnchantParticle extends AnimatedParticle {
    double initialVelocityX;
    double initialVelocityY;
    double initialVelocityZ;

    BreakEnchantParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, spriteProvider, 0F);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.initialVelocityX = velocityX;
        this.initialVelocityY = velocityY;
        this.initialVelocityZ = velocityZ;
        this.maxAge = 50 + this.random.nextInt(22);
        this.updateSprite(spriteProvider);
    }

    public static double particleSpeed(double velocity, double initialVelocity) {
        return velocity - initialVelocity / 60;
    }

    @Override
    public void tick() {
        this.velocityX = particleSpeed(this.velocityX, this.initialVelocityX);
        this.velocityY = particleSpeed(this.velocityY, this.initialVelocityY);
        this.velocityZ = particleSpeed(this.velocityZ, this.initialVelocityZ);
        super.tick();
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(
                SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, Random random
        ) {
            return new BreakEnchantParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}
