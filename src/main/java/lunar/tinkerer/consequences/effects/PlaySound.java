package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.stream.IntStream;

public record PlaySound(Holder<SoundEvent> soundEvent, float volume, int count) implements ConsequenceEffect {
    public static MapCodec<PlaySound> CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
            SoundEvent.CODEC.fieldOf("sound").forGetter(PlaySound::soundEvent),
            Codec.FLOAT.optionalFieldOf("volume", 1f).forGetter(PlaySound::volume),
            Codec.INT.optionalFieldOf("count", 1).forGetter(PlaySound::count)
        ).apply(i, PlaySound::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
        IntStream.range(0, this.count).forEach(_ ->
            info.world().playSound(null, info.tablePos(), soundEvent.value(), SoundSource.BLOCKS, volume, info.world().getRandom().nextFloat() * 0.1f + 0.9f)
        );
        return ItemStack.EMPTY;
    }
}
