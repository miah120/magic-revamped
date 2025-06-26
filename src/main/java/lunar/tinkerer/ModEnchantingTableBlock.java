package lunar.tinkerer;

import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ModEnchantingTableBlock extends EnchantingTableBlock {
    public ModEnchantingTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ModEnchantingTableBlockEntity(pos, state);
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
