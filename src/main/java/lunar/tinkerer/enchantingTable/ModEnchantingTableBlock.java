package lunar.tinkerer.enchantingTable;

import lunar.tinkerer.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModEnchantingTableBlock extends EnchantingTableBlock {
    public static List<BlockPos> POWER_PROVIDER_OFFSETS = BlockPos
            .betweenClosedStream(-3, 0, -3, 3, 1, 3)
            .filter(
                    pos -> Math.abs(pos.getX()) > 1 || Math.abs(pos.getZ()) > 1
            ).map(BlockPos::immutable).toList();

    public static List<BlockPos> DECORATION_OFFSETS = BlockPos
            .betweenClosedStream(-3, -1, -3, 3, 1, 3)
            .map(BlockPos::immutable).toList();

    public ModEnchantingTableBlock(Properties settings) {
        super(settings);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModEnchantingTableBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        for (BlockPos blockPos : POWER_PROVIDER_OFFSETS) {
            if (random.nextInt(12) != 0 || !EnchantingTableBlock.isValidBookShelf(world, pos, blockPos)) continue;
            world.addParticle(ParticleTypes.ENCHANT, (double)pos.getX() + 0.5, (double)pos.getY() + 2.0, (double)pos.getZ() + 0.5, (double)((float)blockPos.getX() + random.nextFloat()) - 0.5, (float)blockPos.getY() - random.nextFloat() - 1.0f, (double)((float)blockPos.getZ() + random.nextFloat()) - 0.5);
        }
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide() ? ModEnchantingTableBlock.createTickerHelper(type, ModBlockEntities.ENCHANTING_TABLE_BLOCK_ENTITY, ModEnchantingTableBlockEntity::tick) : null;
    }

    @Override
    @Nullable
    protected MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ModEnchantingTableBlockEntity) {
            Component text = ((Nameable)(blockEntity)).getDisplayName();
            return new SimpleMenuProvider((syncId, inventory, player) -> new ModEnchantmentScreenHandler(syncId, inventory, ContainerLevelAccess.create(world, pos)), text);
        }
        return null;
    }
}
