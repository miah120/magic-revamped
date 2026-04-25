package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public record ApplyCurse() implements ConsequenceEffect {
    @Override
    public ItemStack run(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        var optional = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.CURSE);
        if (optional.isEmpty()) return ItemStack.EMPTY;
        Holder<Enchantment> curse = EnchantmentHelper
            .selectEnchantment(player.getRandom(), stack, 25, optional.get().stream())
            .getFirst()
            .enchantment();
        stack.enchant(curse, 1);
        return stack;
    }
}
