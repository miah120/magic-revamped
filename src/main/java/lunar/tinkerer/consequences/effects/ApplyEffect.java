package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public record ApplyEffect(Holder<MobEffect> statusEffect, int minTicks, int maxTicks, int amplifier) implements ConsequenceEffect {
    public static MapCodec<ApplyEffect> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    MobEffect.CODEC.fieldOf("effect").forGetter(ApplyEffect::statusEffect),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("min_ticks").forGetter(ApplyEffect::minTicks),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_ticks").forGetter(ApplyEffect::maxTicks),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("amplifier").forGetter(ApplyEffect::amplifier)
            ).apply(i, ApplyEffect::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        int duration = world.getRandom().nextIntBetweenInclusive(minTicks, maxTicks);
        MobEffectInstance effect = new MobEffectInstance(statusEffect, duration, amplifier);
        player.addEffect(effect, player);
        return ItemStack.EMPTY;
    }
}
