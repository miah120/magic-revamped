package lunar.tinkerer.mixin;

import lunar.tinkerer.MagicRevamped;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {
	@Inject(at = @At("HEAD"), method = "updateResult", cancellable = true)
	private void init(CallbackInfo info) {
		this.myUpdateResult();
		info.cancel();
		// This code is injected into the start of MinecraftServer.loadWorld()V
	}

	@Unique
	public void myUpdateResult() {
		MagicRevamped.LOGGER.info("We anvilling");
		AnvilScreenHandler thisObject = (AnvilScreenHandler) (Object)this;
		AnvilScreenHandlerAccessor accessor = (AnvilScreenHandlerAccessor) thisObject;
		ForgingScreenHandlerAccessor forgingAccessor = (ForgingScreenHandlerAccessor) thisObject;
		ItemStack input = forgingAccessor.getInput().getStack(0);
		accessor.setKeepSecondSlot(false);
		accessor.getLevelCost().set(1);
		int k;
		int i = 0;
		int j = 0;
		if (input.isEmpty()) {
			forgingAccessor.getOutput().setStack(0, ItemStack.EMPTY);
			accessor.getLevelCost().set(0);
			return;
		}
		ItemStack itemStack2 = input.copy();
		ItemStack sacrifice = forgingAccessor.getInput().getStack(1);
		long l = (long) input.getOrDefault(DataComponentTypes.REPAIR_COST, 0)
				+ (long) sacrifice.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
		accessor.setRepairItemUsage(0);
		if (!sacrifice.isEmpty()) {
			if (itemStack2.isDamageable() && input.canRepairWith(sacrifice)) {
				int m;
				k = Math.min(itemStack2.getDamage(), itemStack2.getMaxDamage() / 4);
				if (k <= 0) {
					forgingAccessor.getOutput().setStack(0, ItemStack.EMPTY);
					accessor.getLevelCost().set(0);
					return;
				}
				for (m = 0; k > 0 && m < sacrifice.getCount(); ++m) {
					int n = itemStack2.getDamage() - k;
					itemStack2.setDamage(n);
					++i;
					k = Math.min(itemStack2.getDamage(), itemStack2.getMaxDamage() / 4);
				}
				accessor.setRepairItemUsage(m);
			} else {
				if (!(itemStack2.isOf(sacrifice.getItem()) && itemStack2.isDamageable())) {
					forgingAccessor.getOutput().setStack(0, ItemStack.EMPTY);
					accessor.getLevelCost().set(0);
					return;
				}
				if (itemStack2.isDamageable()) {
					int k2 = input.getMaxDamage() - input.getDamage();
					int m = sacrifice.getMaxDamage() - sacrifice.getDamage();
					int n = m + itemStack2.getMaxDamage() * 12 / 100;
					int o = k2 + n;
					int p = itemStack2.getMaxDamage() - o;
					if (p < 0) {
						p = 0;
					}
					if (p < itemStack2.getDamage()) {
						itemStack2.setDamage(p);
						i += 2;
					}
				}
			}
		}
		if (accessor.getNewItemName() == null || StringHelper.isBlank(accessor.getNewItemName())) {
			if (input.contains(DataComponentTypes.CUSTOM_NAME)) {
				j = 1;
				i += j;
				itemStack2.remove(DataComponentTypes.CUSTOM_NAME);
			}
		} else if (!accessor.getNewItemName().equals(input.getName().getString())) {
			j = 1;
			i += j;
			itemStack2.set(DataComponentTypes.CUSTOM_NAME, Text.literal(accessor.getNewItemName()));
		}
		int t = i <= 0 ? 0 : (int) MathHelper.clamp(l + (long)i, 0L, Integer.MAX_VALUE);
		accessor.getLevelCost().set(t);
		if (i <= 0) {
			itemStack2 = ItemStack.EMPTY;
		}
		if (j == i && j > 0) {
			if (accessor.getLevelCost().get() >= 40) {
				accessor.getLevelCost().set(39);
			}
			accessor.setKeepSecondSlot(true);
		}
		if (accessor.getLevelCost().get() >= 40 && !forgingAccessor.getPlayer().isInCreativeMode()) {
			itemStack2 = ItemStack.EMPTY;
		}
		forgingAccessor.getOutput().setStack(0, itemStack2);
		thisObject.sendContentUpdates();
	}

}