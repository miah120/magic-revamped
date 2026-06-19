package lunar.tinkerer.consequences;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.consequences.effects.*;
import lunar.tinkerer.registry.ModRegistries;
import lunar.tinkerer.registry.ModRegistryKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.function.UnaryOperator;

public interface ConsequenceEffect {
    Codec<ConsequenceEffect> CODEC = ModRegistries.CONSEQUENCE_EFFECT.byNameCodec()
        .dispatch(ConsequenceEffect::codec, c -> c);

    MapCodec<? extends ConsequenceEffect> codec();

    ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack);

    static Object bootstrap(final Registry<MapCodec<? extends ConsequenceEffect>> registry) {
        Registry.register(registry, MagicRevamped.identifier("apply_effect"), ApplyEffect.CODEC);
        Registry.register(registry, MagicRevamped.identifier("teleport"), Teleport.CODEC);
        Registry.register(registry, MagicRevamped.identifier("transform_area"), TransformArea.CODEC);
        Registry.register(registry, MagicRevamped.identifier("transform_block"), TransformBlock.CODEC);
        Registry.register(registry, MagicRevamped.identifier("summon_entity"), SummonEntity.CODEC);
        Registry.register(registry, MagicRevamped.identifier("summon_lightning"), SummonLightning.CODEC);
        Registry.register(registry, MagicRevamped.identifier("play_sound"), PlaySound.CODEC);
        Registry.register(registry, MagicRevamped.identifier("explosion"), Explosion.CODEC);
        Registry.register(registry, MagicRevamped.identifier("enchant_success"), EnchantSuccess.CODEC);
        return Registry.register(registry, MagicRevamped.identifier("apply_curse"), ApplyCurse.CODEC);
    }
}
