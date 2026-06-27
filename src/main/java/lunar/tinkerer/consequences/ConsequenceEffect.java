package lunar.tinkerer.consequences;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.consequences.effects.*;
import lunar.tinkerer.registry.ModRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;

public interface ConsequenceEffect {
    Codec<ConsequenceEffect> CODEC = ModRegistries.CONSEQUENCE_EFFECT.byNameCodec()
        .dispatch(ConsequenceEffect::codec, c -> c);

    MapCodec<? extends ConsequenceEffect> codec();

    ItemStack apply(Consequence.RunInfo info);

    static Object bootstrap(final Registry<MapCodec<? extends ConsequenceEffect>> registry) {
        Registry.register(registry, MagicRevamped.id("run_function"), RunFunction.CODEC);
        Registry.register(registry, MagicRevamped.id("apply_effect"), ApplyEffect.CODEC);
        Registry.register(registry, MagicRevamped.id("teleport"), Teleport.CODEC);
        Registry.register(registry, MagicRevamped.id("transform_area"), TransformArea.CODEC);
        Registry.register(registry, MagicRevamped.id("transform_block"), TransformBlock.CODEC);
        Registry.register(registry, MagicRevamped.id("transform_decoration"), TransformDecoration.CODEC);
        Registry.register(registry, MagicRevamped.id("summon_entity"), SummonEntity.CODEC);
        Registry.register(registry, MagicRevamped.id("summon_lightning"), SummonLightning.CODEC);
        Registry.register(registry, MagicRevamped.id("play_sound"), PlaySound.CODEC);
        Registry.register(registry, MagicRevamped.id("spawn_particles"), SpawnParticles.CODEC);
        Registry.register(registry, MagicRevamped.id("explosion"), Explosion.CODEC);
        Registry.register(registry, MagicRevamped.id("enchant_success"), EnchantSuccess.CODEC);
        Registry.register(registry, MagicRevamped.id("enchant_flower"), EnchantFlower.CODEC);
        return Registry.register(registry, MagicRevamped.id("apply_enchantment"), ApplyEnchantment.CODEC);
    }
}
