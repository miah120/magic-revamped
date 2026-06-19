package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record SummonEntity<T extends Entity>(EntityType<T> entityType, int count) implements ConsequenceEffect {
    public static MapCodec<SummonEntity<?>> CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
            EntityType.CODEC.fieldOf("entity_type").forGetter(SummonEntity::entityType),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("count", 1).forGetter(SummonEntity::count)
        ).apply(i, SummonEntity::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(ServerLevel world, BlockPos blockPos, ServerPlayer player, CraftingContainer input, ItemStack stack) {
        Stream<Entity> entities = IntStream.range(0, count)
            .mapToObj(i -> entityType.spawn(world, player.blockPosition(), EntitySpawnReason.MOB_SUMMONED));
        world.addWorldGenChunkEntities(entities);
        return ItemStack.EMPTY;
    }
}
