package lunar.tinkerer.EnchantmentTable;

import lunar.tinkerer.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModEnchantingTableBlock extends EnchantingTableBlock {
    public static List<BlockPos> POWER_PROVIDER_OFFSETS = BlockPos
            .stream(-3, 0, -3, 3, 1, 3)
            .filter(
                    pos -> Math.abs(pos.getX()) > 1 || Math.abs(pos.getZ()) > 1
            ).map(BlockPos::toImmutable).toList();

    public ModEnchantingTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ModEnchantingTableBlockEntity(pos, state);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        for (BlockPos blockPos : POWER_PROVIDER_OFFSETS) {
            if (random.nextInt(16) != 0 || !EnchantingTableBlock.canAccessPowerProvider(world, pos, blockPos)) continue;
            world.addParticleClient(ParticleTypes.ENCHANT, (double)pos.getX() + 0.5, (double)pos.getY() + 2.0, (double)pos.getZ() + 0.5, (double)((float)blockPos.getX() + random.nextFloat()) - 0.5, (float)blockPos.getY() - random.nextFloat() - 1.0f, (double)((float)blockPos.getZ() + random.nextFloat()) - 0.5);
        }
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? ModEnchantingTableBlock.validateTicker(type, ModBlockEntities.ENCHANTING_TABLE_BLOCK_ENTITY, ModEnchantingTableBlockEntity::tick) : null;
    }

    @Override
    @Nullable
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ModEnchantingTableBlockEntity) {
            Text text = ((Nameable)(blockEntity)).getDisplayName();
            return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> new ModEnchantmentScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)), text);
        }
        return null;
    }
}
