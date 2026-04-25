package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public record ApplyEffect(Holder<MobEffect> statusEffect) implements ConsequenceEffect {
    @Override
    public ItemStack run(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        player.addEffect(
            new MobEffectInstance(statusEffect, world.getRandom().nextIntBetweenInclusive(600, 1200)),
            player
        );
        return ItemStack.EMPTY;
    }
}
