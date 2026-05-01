package lunar.tinkerer.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public class AnvilScreenHandlerMixin {
	@Inject(at = @At("HEAD"), method = "createResult", cancellable = true)
	private void init(CallbackInfo info) {
		this.myUpdateResult();
		info.cancel();
		// This code is injected into the start of MinecraftServer.loadWorld()V
	}

	@Unique
	public void myUpdateResult() {
		AnvilMenu thisObject = (AnvilMenu) (Object)this;
		AnvilScreenHandlerAccessor accessor = (AnvilScreenHandlerAccessor) thisObject;
		ForgingScreenHandlerAccessor forgingAccessor = (ForgingScreenHandlerAccessor) thisObject;
		ItemStack input = forgingAccessor.getInputSlots().getItem(0);
		accessor.setKeepSecondSlot(false);
		accessor.getCost().set(1);
		int k;
		int i = 0;
		if (input.isEmpty()) {
			forgingAccessor.getResultSlots().setItem(0, ItemStack.EMPTY);
			accessor.getCost().set(0);
			return;
		}
		ItemStack result = input.copy();
		ItemStack sacrifice = forgingAccessor.getInputSlots().getItem(1);
		long l = (long) input.getOrDefault(DataComponents.REPAIR_COST, 0)
				+ (long) sacrifice.getOrDefault(DataComponents.REPAIR_COST, 0);
		accessor.setRepairItemUsage(0);
		if (!sacrifice.isEmpty()) {
			if (result.isDamageableItem() && input.isValidRepairItem(sacrifice)) {
				int m;
				k = Math.min(result.getDamageValue(), result.getMaxDamage() / 2);
				if (k <= 0) {
					forgingAccessor.getResultSlots().setItem(0, ItemStack.EMPTY);
					accessor.getCost().set(0);
					return;
				}
				for (m = 0; k > 0 && m < sacrifice.getCount(); ++m) {
					int n = result.getDamageValue() - k;
					result.setDamageValue(n);
					i += 5;
					k = Math.min(result.getDamageValue(), result.getMaxDamage() / 2);
				}
				accessor.setRepairItemUsage(m);
			} else {
				if (!(result.is(sacrifice.getItem()) && result.isDamageableItem())) {
					forgingAccessor.getResultSlots().setItem(0, ItemStack.EMPTY);
					accessor.getCost().set(0);
					return;
				}
				if (result.isDamageableItem()) {
					int k2 = input.getMaxDamage() - input.getDamageValue();
					int m = sacrifice.getMaxDamage() - sacrifice.getDamageValue();
					int n = m + result.getMaxDamage() * 12 / 100;
					int o = k2 + n;
					int p = result.getMaxDamage() - o;
					if (p < 0) {
						p = 0;
					}
					if (p < result.getDamageValue()) {
						result.setDamageValue(p);
						i += 2;
					}
				}
			}
			if (result.isDamageableItem()) {
				result.set(DataComponents.MAX_DAMAGE, result.getMaxDamage() + 1);
			}
		}
		if (accessor.getItemName() == null || StringUtil.isBlank(accessor.getItemName())) {
			if (input.has(DataComponents.CUSTOM_NAME)) {
				i += 1;
				result.remove(DataComponents.CUSTOM_NAME);
			}
		} else if (!accessor.getItemName().equals(input.getHoverName().getString())) {
			i += 1;
			result.set(DataComponents.CUSTOM_NAME, Component.literal(accessor.getItemName()));
		}
		int t = i <= 0 ? 0 : (int) Mth.clamp(l + (long)i, 0L, Integer.MAX_VALUE);
		accessor.getCost().set(t);
		if (i <= 0) {
			result = ItemStack.EMPTY;
		}
		forgingAccessor.getResultSlots().setItem(0, result);
		thisObject.broadcastChanges();
	}

}