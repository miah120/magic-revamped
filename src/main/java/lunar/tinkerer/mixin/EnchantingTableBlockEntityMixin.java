package lunar.tinkerer.mixin;

import com.mojang.serialization.Codec;
import lunar.tinkerer.MagicRevamped;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantingTableBlockEntity.class)
public class EnchantingTableBlockEntityMixin {
    @Inject(method = "bookAnimationTick", at = @At("TAIL"))
    private static void bookAnimationTick(Level level, BlockPos worldPosition, BlockState state, EnchantingTableBlockEntity entity, CallbackInfo ci) {
        if (!state.getValue(BlockStateProperties.HAS_BOOK)) {
            entity.open = 0f;
        }
    }

    @Inject(method = "applyImplicitComponents", at = @At("TAIL"))
    protected void applyImplicitComponents(DataComponentGetter components, CallbackInfo ci) {
        EnchantingTableBlockEntity thisObj = (EnchantingTableBlockEntity) (Object) this;
        boolean hasBook = components.getOrDefault(MagicRevamped.DataComponents.HAS_BOOK, true);
        Level level = thisObj.getLevel();
        if (level == null) return;
        level.setBlockAndUpdate(thisObj.getBlockPos(), thisObj.getBlockState().setValue(BlockStateProperties.HAS_BOOK, hasBook));
    }

    @Inject(method = "collectImplicitComponents", at = @At("TAIL"))
    protected void collectImplicitComponents(DataComponentMap.Builder components, CallbackInfo ci) {
        EnchantingTableBlockEntity thisObj = (EnchantingTableBlockEntity) (Object) this;
        components.set(MagicRevamped.DataComponents.HAS_BOOK, thisObj.getBlockState().getValue(BlockStateProperties.HAS_BOOK));
    }
}
