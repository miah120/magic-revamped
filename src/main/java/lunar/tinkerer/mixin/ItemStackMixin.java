package lunar.tinkerer.mixin;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.ModItems;
import lunar.tinkerer.RuneItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.World;
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
            method = "damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V",
            cancellable = true
    )
    private void init(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack thisObj = (ItemStack)(Object) this;
        World world = entity.getEntityWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            ci.cancel();
            return;
        }
        if (!(entity instanceof ServerPlayerEntity serverPlayerEntity)) {
            ci.cancel();
            return;
        }
        List<RuneItem.LeveledEnchantment> enchantments = RuneItem.getEnchantments(thisObj).toList();
        ItemStack thisCopy = thisObj.copy();
        thisObj.damage(
                amount,
                serverWorld,
                serverPlayerEntity,
                (Item item) -> {
                    RuneItem.LeveledEnchantment chargedEnchant = getChargedEnchant(thisCopy, enchantments, serverWorld);
                    int flux = getFluxValue(thisCopy);
                    if (!enchantments.isEmpty()) {
                        serverWorld.spawnParticles(
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
                                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                                SoundCategory.BLOCKS,
                                1F,
                                2F
                        );
                        serverWorld.playSound(
                                null,
                                serverPlayerEntity.getX(),
                                serverPlayerEntity.getY(),
                                serverPlayerEntity.getZ(),
                                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                                SoundCategory.BLOCKS,
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
                            serverPlayerEntity.dropItem(chargedRune, false);
                        }
                        if(itemStack.isEmpty()) return;
                        serverPlayerEntity.dropItem(itemStack, false);
                    });
                    entity.sendEquipmentBreakStatus(item, slot);
                });
        ci.cancel();
    }

    @Unique
    RuneItem.LeveledEnchantment getChargedEnchant(ItemStack itemStack, List<RuneItem.LeveledEnchantment> enchantments, ServerWorld world) {
        if (!itemStack.isIn(ModItems.DROPS_CHARGED_RUNE)) {
            return null;
        }
        List<RuneItem.LeveledEnchantment> viable = enchantments.stream().filter(
                leveledEnchantment -> leveledEnchantment.enchantment().value().getMaxLevel() > 1
        ).toList();
        if (viable.isEmpty()) {
            return null;
        }
        int pickedSlot = world.random.nextInt(viable.size());
        return viable.get(pickedSlot);
    }

    @Unique
    int getFluxValue(ItemStack itemStack) {
        var repairMaterial = itemStack.get(DataComponentTypes.REPAIRABLE);
        if (repairMaterial == null) return 8;
        if (repairMaterial.matches(new ItemStack(Items.DIAMOND))) return 1;
        if (repairMaterial.matches(new ItemStack(Items.GOLD_INGOT))) return 4;
        if (repairMaterial.matches(new ItemStack(Items.IRON_INGOT))) return 6;
        if (repairMaterial.matches(new ItemStack(Items.COPPER_INGOT))) return 6;
        if (repairMaterial.matches(new ItemStack(Items.COBBLESTONE))) return 7;
        return 8;
    }
}
