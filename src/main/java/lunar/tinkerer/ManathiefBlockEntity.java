/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package lunar.tinkerer;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ManathiefBlockEntity
        extends BlockEntity {
    public int ticks;
    public float bookRotation;
    public float lastBookRotation;
    public float targetBookRotation;
    public float leafRotation;
    private static final RandomSource RANDOM = RandomSource.create();

    public ManathiefBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANATHIEF_BLOCK_ENTITY, pos, state);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, ManathiefBlockEntity blockEntity) {
        blockEntity.lastBookRotation = blockEntity.bookRotation;
        Player playerEntity = world.getNearestPlayer((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, 8.0, false);
        if (playerEntity != null) {
            double d = playerEntity.getX() - ((double) pos.getX() + 0.5);
            double e = playerEntity.getZ() - ((double) pos.getZ() + 0.5);
            blockEntity.targetBookRotation = (float) Mth.atan2(e, d);
        }
        blockEntity.bookRotation = normalizeRotation(blockEntity.bookRotation);
        blockEntity.targetBookRotation = normalizeRotation(blockEntity.targetBookRotation);
        float g = normalizeRotation(blockEntity.targetBookRotation - blockEntity.bookRotation);
        blockEntity.bookRotation += g * 0.4f;
        ++blockEntity.ticks;
    }

    public static float normalizeRotation(float x) {
        float base = x % (float) (Math.PI * 2);
        return base < Math.PI ? base : base - (float) (Math.PI * 2);
    }
}

