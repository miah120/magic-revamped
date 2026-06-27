package lunar.tinkerer.mixin;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.RuneItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
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
    private void hurtAndBreak(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack thisObj = (ItemStack)(Object) this;
        Level world = entity.level();
        ci.cancel();
        if (!(world instanceof ServerLevel serverWorld)) return;
        if (!(entity instanceof ServerPlayer serverPlayerEntity)) return;
        List<RuneItem.LeveledEnchantment> enchantments = RuneItem.getEnchantments(thisObj).toList();
        ItemStack thisCopy = thisObj.copy();
        thisObj.hurtAndBreak(amount, serverWorld, serverPlayerEntity, (Item item) -> {
            onEnchantedItemBreak(serverWorld, thisCopy, serverPlayerEntity, enchantments);
            entity.onEquippedItemBroken(item, slot);
        });
    }

    @Unique
    void onEnchantedItemBreak(ServerLevel level, ItemStack thisCopy, ServerPlayer player, List<RuneItem.LeveledEnchantment> enchantments) {
        RuneItem.LeveledEnchantment chargedEnchant = getChargedEnchant(thisCopy, enchantments, level);
        RuneItem.Flux flux = new RuneItem.Flux(getFluxValue(thisCopy));
        if (enchantments.isEmpty()) return;
        level.sendParticles(MagicRevamped.BREAK_ENCHANT_PARTICLE, player.getX(), player.getEyeY(), player.getZ(), 500, 0,0,0, 1);
        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1F, 2F);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 100F, 0.5F);
        enchantments.forEach(leveledEnchantment -> {
            ItemStack itemStack = RuneItem.makeRune(leveledEnchantment.enchantment(), true, false, leveledEnchantment.level(), flux);
            if(leveledEnchantment == chargedEnchant) {
                ItemStack chargedRune = itemStack.split(1);
                chargedRune.set(MagicRevamped.DataComponents.CHARGED, Unit.INSTANCE);
                player.drop(chargedRune, false);
            }
            if(itemStack.isEmpty()) return;
            player.drop(itemStack, false);
        });
    }

    @Unique
    RuneItem.LeveledEnchantment getChargedEnchant(ItemStack itemStack, List<RuneItem.LeveledEnchantment> enchantments, ServerLevel world) {
        if (!itemStack.is(MagicRevamped.Items.DROPS_CHARGED_RUNE)) return null;
        List<RuneItem.LeveledEnchantment> viable = enchantments.stream()
            .filter(enchant -> enchant.enchantment().value().getMaxLevel() > 1)
            .toList();
        return Util.getRandomSafe(viable, world.getRandom()).orElse(null);
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
