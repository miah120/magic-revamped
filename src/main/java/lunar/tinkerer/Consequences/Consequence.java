package lunar.tinkerer.Consequences;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.registry.ModRegistryKeys;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;


public record Consequence(
        String description,
        Ingredient decoration,
        Integer weight
) {
    public static final Consequence EMPTY = new Consequence(
        "default",
        Ingredient.ofItem(Items.BARRIER),
        0
    );
    public static final MapCodec<Consequence> CODEC = RecordCodecBuilder
        .mapCodec(instance -> instance.group(
                Codec.STRING
                        .optionalFieldOf("description", "")
                        .forGetter(consequence -> consequence.description),
                Ingredient.CODEC
                        .fieldOf("decoration")
                        .forGetter(consequence -> consequence.decoration),
                Codec.INT
                    .fieldOf("weight")
                    .forGetter(consequence -> consequence.weight)
                //TODO: Implement consequence effects
            ).apply(instance, Consequence::new)
        );

    public static final Codec<RegistryEntry<Consequence>> ENTRY_CODEC = RegistryFixedCodec
            .of(ModRegistryKeys.CONSEQUENCE);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<Consequence>> ENTRY_PACKET_CODEC = PacketCodecs
        .registryEntry(ModRegistryKeys.CONSEQUENCE);

    public boolean test(Block block) {
        return this.decoration.test(new ItemStack(block.asItem()));
    }
}
