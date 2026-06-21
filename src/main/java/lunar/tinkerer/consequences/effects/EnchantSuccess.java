package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.MapCodec;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.world.item.ItemStack;

public record EnchantSuccess() implements ConsequenceEffect {
    public static final MapCodec<EnchantSuccess> CODEC = MapCodec.unit(new EnchantSuccess());

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
        return info.stack();
    }
}
