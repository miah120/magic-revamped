package lunar.tinkerer.consequences;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.consequences.effects.ApplyCurse;
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

//    MapCodec<?> APPLY_CURSE = register("apply_curse", ApplyCurse.CODEC);

    MapCodec<? extends ConsequenceEffect> codec();

    ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack);

    static <T extends ConsequenceEffect> MapCodec<T> register(String id, MapCodec<T> codec) {
        return Registry.register(ModRegistries.CONSEQUENCE_EFFECT, MagicRevamped.identifier(id), codec);
    }

    static Object bootstrap(final Registry<MapCodec<? extends ConsequenceEffect>> registry) {
        return Registry.register(registry, MagicRevamped.identifier("apply_curse"), ApplyCurse.CODEC);
    }
}
