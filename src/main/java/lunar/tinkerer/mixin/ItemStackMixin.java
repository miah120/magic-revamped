package lunar.tinkerer.mixin;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.ModItems;
import lunar.tinkerer.RuneItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(
            at = @At("HEAD"),
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            cancellable = true
    )
    private void init(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack thisObj = (ItemStack)(Object) this;
        Level world = entity.level();
        if (!(world instanceof ServerLevel serverWorld)) {
            ci.cancel();
            return;
        }
        if (!(entity instanceof ServerPlayer serverPlayerEntity)) {
            ci.cancel();
            return;
        }
        List<RuneItem.LeveledEnchantment> enchantments = RuneItem.getEnchantments(thisObj).toList();
        ItemStack thisCopy = thisObj.copy();
        thisObj.hurtAndBreak(
                amount,
                serverWorld,
                serverPlayerEntity,
                (Item item) -> {
                    RuneItem.LeveledEnchantment chargedEnchant = getChargedEnchant(thisCopy, enchantments, serverWorld);
                    int flux = getFluxValue(thisCopy);
                    if (!enchantments.isEmpty()) {
                        serverWorld.sendParticles(
                                MagicRevamped.BREAK_ENCHANT_PARTICLE,
                                serverPlayerEntity.getX(),
                                serverPlayerEntity.getEyeY(),
                                serverPlayerEntity.getZ(),
                                500,
                                0,0,0,
                                1
                        );
                        serverWorld.playSound(
                                null,
                                serverPlayerEntity.getX(),
                                serverPlayerEntity.getY(),
                                serverPlayerEntity.getZ(),
                                SoundEvents.ENCHANTMENT_TABLE_USE,
                                SoundSource.BLOCKS,
                                1F,
                                2F
                        );
                        serverWorld.playSound(
                                null,
                                serverPlayerEntity.getX(),
                                serverPlayerEntity.getY(),
                                serverPlayerEntity.getZ(),
                                SoundEvents.AMETHYST_BLOCK_CHIME,
                                SoundSource.BLOCKS,
                                100F,
                                0.5F
                        );
                    }
                    enchantments.forEach(leveledEnchantment -> {
                        ItemStack itemStack = new ItemStack(ModItems.RUNE, leveledEnchantment.level());
                        itemStack.set(ModItems.OPEN, Unit.INSTANCE);
                        itemStack.set(ModItems.ENCHANTMENT, leveledEnchantment.enchantment());
                        itemStack.set(ModItems.FLUX, flux);
                        if(leveledEnchantment == chargedEnchant) {
                            ItemStack chargedRune = itemStack.split(1);
                            chargedRune.set(ModItems.CHARGED, Unit.INSTANCE);
                            serverPlayerEntity.drop(chargedRune, false);
                        }
                        if(itemStack.isEmpty()) return;
                        serverPlayerEntity.drop(itemStack, false);
                    });
                    entity.onEquippedItemBroken(item, slot);
                });
        ci.cancel();
    }

    @Unique
    RuneItem.LeveledEnchantment getChargedEnchant(ItemStack itemStack, List<RuneItem.LeveledEnchantment> enchantments, ServerLevel world) {
        if (!itemStack.is(ModItems.DROPS_CHARGED_RUNE)) {
            return null;
        }
        List<RuneItem.LeveledEnchantment> viable = enchantments.stream().filter(
                leveledEnchantment -> leveledEnchantment.enchantment().value().getMaxLevel() > 1
        ).toList();
        if (viable.isEmpty()) {
            return null;
        }
        int pickedSlot = world.getRandom().nextInt(viable.size());
        return viable.get(pickedSlot);
    }

    @Unique
    int getFluxValue(ItemStack itemStack) {
        var repairMaterial = itemStack.get(DataComponents.REPAIRABLE);
        if (repairMaterial == null) return 8;
        if (repairMaterial.isValidRepairItem(new ItemStack(Items.DIAMOND))) return 1;
        if (repairMaterial.isValidRepairItem(new ItemStack(Items.GOLD_INGOT))) return 4;
        if (repairMaterial.isValidRepairItem(new ItemStack(Items.IRON_INGOT))) return 6;
        if (repairMaterial.isValidRepairItem(new ItemStack(Items.COPPER_INGOT))) return 6;
        if (repairMaterial.isValidRepairItem(new ItemStack(Items.COBBLESTONE))) return 7;
        return 8;
    }
}
