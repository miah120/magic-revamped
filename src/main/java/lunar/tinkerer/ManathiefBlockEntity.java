/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package lunar.tinkerer;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ManathiefBlockEntity
        extends BlockEntity {
    public int ticks;
    public float bookRotation;
    public float lastBookRotation;
    public float targetBookRotation;
    public float leafRotation;
    private static final Random RANDOM = Random.create();

    public ManathiefBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANATHIEF_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ManathiefBlockEntity blockEntity) {
        blockEntity.lastBookRotation = blockEntity.bookRotation;
        PlayerEntity playerEntity = world.getClosestPlayer((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, 8.0, false);
        if (playerEntity != null) {
            double d = playerEntity.getX() - ((double) pos.getX() + 0.5);
            double e = playerEntity.getZ() - ((double) pos.getZ() + 0.5);
            blockEntity.targetBookRotation = (float) MathHelper.atan2(e, d);
        } else {
            blockEntity.targetBookRotation += 0.02f;
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

