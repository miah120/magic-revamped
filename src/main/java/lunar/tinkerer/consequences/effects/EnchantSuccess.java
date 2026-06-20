package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;

public record EnchantSuccess() implements ConsequenceEffect {
    public static final MapCodec<EnchantSuccess> CODEC = MapCodec.unit(new EnchantSuccess());

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
        return info.stack();
    }
}
