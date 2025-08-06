package lunar.tinkerer;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class ManathiefBlock extends TallPlantBlock {
    public static final MapCodec<ManathiefBlock> CODEC = createCodec(ManathiefBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = TallPlantBlock.HALF;
    private static final VoxelShape LOWER_COLLISION_SHAPE = Block.createColumnShape(10.0, -1.0, 5.0);
    private final Function<BlockState, VoxelShape> shapeFunction = this.createShapeFunction();

    @Override
    public MapCodec<ManathiefBlock> getCodec() {
        return CODEC;
    }

    public ManathiefBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    private Function<BlockState, VoxelShape> createShapeFunction() {
        return this.createShapeFunction(state -> switch (state.get(HALF)) {
            case LOWER -> Block.createColumnShape(10, -1.0, 16);
            case UPPER -> Block.createColumnShape(10, 0.0, 15);
        });
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return LOWER_COLLISION_SHAPE;
        } else {
            return VoxelShapes.empty();
        }
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
        if (world instanceof ServerWorld serverWorld && entity instanceof RavagerEntity && serverWorld.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            entity.setOnFireFor(5f);
        }
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return false;
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return false;
    }

}
