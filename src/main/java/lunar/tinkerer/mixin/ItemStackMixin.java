package lunar.tinkerer.mixin;

import lunar.tinkerer.ModItems;
import lunar.tinkerer.RuneItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(
            at = @At("HEAD"),
            method = "damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V",
            cancellable = true
    )
    private void init(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack thisObj = (ItemStack)(Object) this;
        World world = entity.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            ci.cancel();
            return;
        }
        if (!(entity instanceof ServerPlayerEntity serverPlayerEntity)) {
            ci.cancel();
            return;
        }
        List<RuneItem.LeveledEnchantment> enchantments = RuneItem.getEnchantments(thisObj).toList();
        thisObj.damage(
                amount,
                serverWorld,
                serverPlayerEntity,
                (Item item) -> {
                    enchantments.forEach(leveledEnchantment -> {
                        ItemStack itemStack = new ItemStack(ModItems.RUNE, leveledEnchantment.level());
                        itemStack.set(ModItems.OPEN, Unit.INSTANCE);
                        itemStack.set(ModItems.ENCHANTMENT, leveledEnchantment.enchantment());
                        serverPlayerEntity.dropItem(itemStack, true);
                    });
                    entity.sendEquipmentBreakStatus(item, slot);
                });
        ci.cancel();
    }
}
