package lunar.tinkerer.Consequences;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.registry.ModRegistryKeys;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;


public record Consequence(String description) {
    public static final MapCodec<Consequence> CODEC = RecordCodecBuilder
        .mapCodec(instance -> instance.group(
                Codec.STRING
                    .optionalFieldOf("description", "")
                    .forGetter(consequence -> consequence.description)
            ).apply(instance, Consequence::new)
        );

    public static final Codec<RegistryEntry<Consequence>> ENTRY_CODEC = RegistryFixedCodec
            .of(ModRegistryKeys.CONSEQUENCE);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<Consequence>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(ModRegistryKeys.CONSEQUENCE);
}
