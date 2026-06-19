package lunar.tinkerer.consequences;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.consequences.effects.ApplyCurse;
import lunar.tinkerer.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.function.UnaryOperator;

public interface ConsequenceEffect {
    Codec<ConsequenceEffect> CODEC = ModRegistries.CONSEQUENCE_EFFECT.byNameCodec()
        .dispatch(c -> c, ConsequenceEffect::codec);

    public static final ConsequenceEffect APPLY_CURSE = register("apply_curse", new ApplyCurse());


    MapCodec<? extends ConsequenceEffect> codec();

    ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack);

    private static ConsequenceEffect register(final String id, final ConsequenceEffect consequenceEffect) {
        return Registry.register(ModRegistries.CONSEQUENCE_EFFECT, MagicRevamped.identifier(id), consequenceEffect);
    }

}
