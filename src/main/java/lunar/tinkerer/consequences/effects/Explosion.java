package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record Explosion(float power, boolean fire) implements ConsequenceEffect {
    public static MapCodec<Explosion> CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
            Codec.FLOAT.optionalFieldOf("power", 5f).forGetter(Explosion::power),
            Codec.BOOL.optionalFieldOf("fire", true).forGetter(Explosion::fire)
        ).apply(i, Explosion::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        world.explode(
            null,
            world.damageSources().magic(),
            null,
            blockPos.getCenter().add(0, 1, 0),
            power,
            fire,
            Level.ExplosionInteraction.BLOCK
        );

        return ItemStack.EMPTY;
    }
}
