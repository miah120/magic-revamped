/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package lunar.tinkerer.enchantingTable;

import lunar.tinkerer.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class ModEnchantingTableBlockEntity
        extends BlockEntity
        implements Nameable {
    public int ticks;
    public float nextPageAngle;
    public float pageAngle;
    public float flipRandom;
    public float flipTurn;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;
    public float bookRotation;
    public float lastBookRotation;
    public float targetBookRotation;
    private static final RandomSource RANDOM = RandomSource.create();
    @Nullable
    private Component customName;

    public ModEnchantingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTING_TABLE_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.customName = ModEnchantingTableBlockEntity.parseCustomNameSafe(view, "CustomName");
    }

    public static void tick(Level world, BlockPos pos, BlockState state, ModEnchantingTableBlockEntity blockEntity) {
        float g;
        blockEntity.pageTurningSpeed = blockEntity.nextPageTurningSpeed;
        blockEntity.lastBookRotation = blockEntity.bookRotation;
        Player playerEntity = world.getNearestPlayer((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 3.0, false);
        if (playerEntity != null) {
            double d = playerEntity.getX() - ((double)pos.getX() + 0.5);
            double e = playerEntity.getZ() - ((double)pos.getZ() + 0.5);
            blockEntity.targetBookRotation = (float)Mth.atan2(e, d);
            blockEntity.nextPageTurningSpeed += 0.1f;
            if (blockEntity.nextPageTurningSpeed < 0.5f || RANDOM.nextInt(40) == 0) {
                float f = blockEntity.flipRandom;
                do {
                    blockEntity.flipRandom += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while (f == blockEntity.flipRandom);
            }
        } else {
            blockEntity.targetBookRotation += 0.02f;
            blockEntity.nextPageTurningSpeed -= 0.1f;
        }
        while (blockEntity.bookRotation >= (float)Math.PI) {
            blockEntity.bookRotation -= (float)Math.PI * 2;
        }
        while (blockEntity.bookRotation < (float)(-Math.PI)) {
            blockEntity.bookRotation += (float)Math.PI * 2;
        }
        while (blockEntity.targetBookRotation >= (float)Math.PI) {
            blockEntity.targetBookRotation -= (float)Math.PI * 2;
        }
        while (blockEntity.targetBookRotation < (float)(-Math.PI)) {
            blockEntity.targetBookRotation += (float)Math.PI * 2;
        }
        for (g = blockEntity.targetBookRotation - blockEntity.bookRotation; g >= (float)Math.PI; g -= (float)Math.PI * 2) {
        }
        while (g < (float)(-Math.PI)) {
            g += (float)Math.PI * 2;
        }
        blockEntity.bookRotation += g * 0.4f;
        blockEntity.nextPageTurningSpeed = Mth.clamp(blockEntity.nextPageTurningSpeed, 0.0f, 1.0f);
        ++blockEntity.ticks;
        blockEntity.pageAngle = blockEntity.nextPageAngle;
        float h = (blockEntity.flipRandom - blockEntity.nextPageAngle) * 0.4f;
        float i = 0.2f;
        h = Mth.clamp(h, -0.2f, 0.2f);
        blockEntity.flipTurn += (h - blockEntity.flipTurn) * 0.9f;
        blockEntity.nextPageAngle += blockEntity.flipTurn;
    }

    @Override
    public Component getName() {
        if (this.customName != null) {
            return this.customName;
        }
        return Component.translatable("container.enchant");
    }

    public void setCustomName(@Nullable Component customName) {
        this.customName = customName;
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.customName = components.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CUSTOM_NAME, this.customName);
    }
}

