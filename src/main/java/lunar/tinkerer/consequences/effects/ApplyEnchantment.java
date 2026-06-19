package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;

public record ApplyEnchantment(HolderSet<Enchantment> enchantments) implements ConsequenceEffect {
    public static MapCodec<ApplyEnchantment> CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).fieldOf("enchantments").forGetter(ApplyEnchantment::enchantments)
        ).apply(i, ApplyEnchantment::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        List<EnchantmentInstance> enchantmentOptions = EnchantmentHelper.selectEnchantment(
                player.getRandom(), stack, 25, this.enchantments.stream()
        );
        if (enchantmentOptions.isEmpty()) return input.getItem(0);
        stack.enchant(enchantmentOptions.getFirst().enchantment(), 1);
        return stack;
    }
}
