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
		if (input.isEmpty()) {
			forgingAccessor.getOutput().setStack(0, ItemStack.EMPTY);
			accessor.getLevelCost().set(0);
			return;
		}
		ItemStack result = input.copy();
		ItemStack sacrifice = forgingAccessor.getInput().getStack(1);
		long l = (long) input.getOrDefault(DataComponentTypes.REPAIR_COST, 0)
				+ (long) sacrifice.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
		accessor.setRepairItemUsage(0);
		if (!sacrifice.isEmpty()) {
			if (result.isDamageable() && input.canRepairWith(sacrifice)) {
				int m;
				k = Math.min(result.getDamage(), result.getMaxDamage() / 2);
				if (k <= 0) {
					forgingAccessor.getOutput().setStack(0, ItemStack.EMPTY);
					accessor.getLevelCost().set(0);
					return;
				}
				for (m = 0; k > 0 && m < sacrifice.getCount(); ++m) {
					int n = result.getDamage() - k;
					result.setDamage(n);
					i += 5;
					k = Math.min(result.getDamage(), result.getMaxDamage() / 2);
				}
				accessor.setRepairItemUsage(m);
			} else {
				if (!(result.isOf(sacrifice.getItem()) && result.isDamageable())) {
					forgingAccessor.getOutput().setStack(0, ItemStack.EMPTY);
					accessor.getLevelCost().set(0);
					return;
				}
				if (result.isDamageable()) {
					int k2 = input.getMaxDamage() - input.getDamage();
					int m = sacrifice.getMaxDamage() - sacrifice.getDamage();
					int n = m + result.getMaxDamage() * 12 / 100;
					int o = k2 + n;
					int p = result.getMaxDamage() - o;
					if (p < 0) {
						p = 0;
					}
					if (p < result.getDamage()) {
						result.setDamage(p);
						i += 2;
					}
				}
			}
		}
		if (accessor.getNewItemName() == null || StringHelper.isBlank(accessor.getNewItemName())) {
			if (input.contains(DataComponentTypes.CUSTOM_NAME)) {
				i += 1;
				result.remove(DataComponentTypes.CUSTOM_NAME);
			}
		} else if (!accessor.getNewItemName().equals(input.getName().getString())) {
			i += 1;
			result.set(DataComponentTypes.CUSTOM_NAME, Text.literal(accessor.getNewItemName()));
		}
		int t = i <= 0 ? 0 : (int) MathHelper.clamp(l + (long)i, 0L, Integer.MAX_VALUE);
		accessor.getLevelCost().set(t);
		if (i <= 0) {
			result = ItemStack.EMPTY;
		}
		forgingAccessor.getOutput().setStack(0, result);
		thisObject.sendContentUpdates();
	}

}