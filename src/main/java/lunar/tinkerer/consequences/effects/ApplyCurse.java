package lunar.tinkerer.consequences.effects;

import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record ApplyCurse() implements ConsequenceEffect {
    @Override
    public ItemStack run(ServerWorld world, BlockPos blockPos, ServerPlayerEntity player, RecipeInputInventory input, ItemStack stack) {
        var optional = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOptional(EnchantmentTags.CURSE);
        if (optional.isEmpty()) return ItemStack.EMPTY;
        RegistryEntry<Enchantment> curse = EnchantmentHelper
            .generateEnchantments(player.getRandom(), stack, 25, optional.get().stream())
            .getFirst()
            .enchantment();
        stack.addEnchantment(curse, 1);
        return stack;
    }
}
