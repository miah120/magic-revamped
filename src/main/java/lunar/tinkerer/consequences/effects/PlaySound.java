package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public record PlaySound(Holder<SoundEvent> soundEvent, float volume) implements ConsequenceEffect {
    public static MapCodec<PlaySound> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    SoundEvent.CODEC.fieldOf("sound").forGetter(PlaySound::soundEvent),
                    Codec.FLOAT.optionalFieldOf("volume", 1f).forGetter(PlaySound::volume)
            ).apply(i, PlaySound::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        world.playSound(null, blockPos, soundEvent.value(), SoundSource.BLOCKS, volume, world.getRandom().nextFloat() * 0.1f + 0.9f);
        return ItemStack.EMPTY;
    }
}
