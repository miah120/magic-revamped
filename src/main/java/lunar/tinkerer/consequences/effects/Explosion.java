package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
    public ItemStack apply(Consequence.RunInfo info) {
        info.world().explode(
            null,
            info.world().damageSources().magic(),
            null,
            Vec3.atCenterOf(info.tablePos().above()),
            power,
            fire,
            Level.ExplosionInteraction.BLOCK
        );

        return ItemStack.EMPTY;
    }
}
