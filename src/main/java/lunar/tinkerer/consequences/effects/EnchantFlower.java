package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.MapCodec;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Supplier;

public record EnchantFlower() implements ConsequenceEffect {
    public static MapCodec<EnchantFlower> CODEC = MapCodec.unit(new EnchantFlower());

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
        Optional<ItemStack> flower = info.decoration()
            .map(BlockInWorld::getState)
            .map(BlockBehaviour.BlockStateBase::getBlock)
            .flatMap(block -> block instanceof FlowerPotBlock pot ? Optional.of(pot) : Optional.empty())
            .map(FlowerPotBlock::getPotted)
            .map(ItemStack::new);
        info.world().registryAccess().lookup(Registries.ENCHANTMENT)
            .flatMap(registry -> registry.getRandom(info.player().getRandom()))
            .flatMap(enchantment -> {
                flower.ifPresent(f -> f.enchant(enchantment, info.player().getRandom().nextInt(1, enchantment.value().getMaxLevel())));
                return flower;
            }).ifPresent(result -> this.drop(info, result));
        return ItemStack.EMPTY;
    }

    public void drop(Consequence.RunInfo info, ItemStack itemStack) {
        Level level = info.world();
        Supplier<Float> r = () -> level.getRandom().nextFloat();
        info.decoration().map(BlockInWorld::getPos).ifPresent(pos -> {
            Vec3 location = Vec3.atCenterOf(pos).add((r.get() - 0.5) * 0.1F, r.get() * 0.05F, (r.get() - 0.5) * 0.1F);
            ItemEntity entity = new ItemEntity(level, location.x, location.y, location.z, itemStack);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        });
    }
}
