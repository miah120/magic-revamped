package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public record ApplyCurse(HolderSet<Enchantment> enchantments) implements ConsequenceEffect {
    public static MapCodec<ApplyCurse> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    RegistryCodecs.homogeneousList(Registries.ENCHANTMENT)
                            .fieldOf("enchantments")
                            .forGetter(ApplyCurse::enchantments)
            ).apply(i, ApplyCurse::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        //TODO: this isn't quite right...
        return this.enchantments.getRandomElement(player.getRandom())
            .filter(curse -> stack.canBeEnchantedWith(curse, EnchantingContext.ACCEPTABLE))
            .map(curse -> {
                stack.enchant(curse, 1);
                return stack;
            })
            .orElse(stack);
    }
}
