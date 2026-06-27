package lunar.tinkerer.manathief;

import com.mojang.serialization.MapCodec;
import lunar.tinkerer.MagicRevamped;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ManathiefBlock extends DoublePlantBlock implements EntityBlock {
    public static final MapCodec<ManathiefBlock> CODEC = simpleCodec(ManathiefBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = DoublePlantBlock.HALF;
    private static final VoxelShape LOWER_COLLISION_SHAPE = Block.column(16.0, 0, 8.0);
    private final Function<BlockState, VoxelShape> shapeFunction = this.createShapeFunction();

    @Override
    public MapCodec<ManathiefBlock> codec() {
        return CODEC;
    }

    public ManathiefBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    private Function<BlockState, VoxelShape> createShapeFunction() {
        return this.getShapeForEachState(state -> switch (state.getValue(HALF)) {
            case LOWER -> Block.column(10, 0, 16);
            case UPPER -> Block.column(10, 0.0, 15);
        });
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return this.shapeFunction.apply(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return LOWER_COLLISION_SHAPE;
        } else {
            return Shapes.empty();
        }
    }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean bl) {
        if (world instanceof ServerLevel serverWorld && entity instanceof Ravager && serverWorld.getGameRules().get(GameRules.MOB_GRIEFING)) {
            entity.igniteForSeconds(5f);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? new ManathiefBlockEntity(pos, state) : null;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide() ? ManathiefBlock.validateTicker(type, ManathiefBlockEntity::tick) : null;
    }

    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> validateTicker(
            BlockEntityType<A> givenType, BlockEntityTicker<? super E> ticker
    ) {
        return MagicRevamped.BlockEntities.MANATHIEF_BLOCK_ENTITY == givenType ? (BlockEntityTicker<A>) ticker : null;
    }
}
