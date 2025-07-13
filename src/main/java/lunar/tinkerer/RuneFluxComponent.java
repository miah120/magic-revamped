package lunar.tinkerer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.Consumer;

public class RuneFluxComponent implements TooltipAppender {
    public static final Codec<RuneFluxComponent> CODEC = Codec.intRange()
    public static final PacketCodec<RegistryByteBuf, ItemEnchantmentsComponent> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.map(Object2IntOpenHashMap::new, Enchantment.ENTRY_PACKET_CODEC, PacketCodecs.VAR_INT),
        component -> component.enchantments,
        ItemEnchantmentsComponent::new
    );


    public int value;

    public RuneFluxComponent(int value) {
        this.value = value;
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(Text.of("" + this.value));
    }
}
