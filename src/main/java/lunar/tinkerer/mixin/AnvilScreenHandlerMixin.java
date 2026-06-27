package lunar.tinkerer.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.world.inventory.AnvilMenu.calculateIncreasedRepairCost;

@Mixin(AnvilMenu.class)
public class AnvilScreenHandlerMixin {
	@Inject(at = @At("HEAD"), method = "createResult", cancellable = true)
	private void init(CallbackInfo info) {
		AnvilMenu thisObj = (AnvilMenu) (Object) this;
		ItemStack input = thisObj.inputSlots.getItem(0);
		var result = this.getResult();
		thisObj.resultSlots.setItem(0, result.result.apply(input.copy()));
		thisObj.cost.set(result.cost);
		thisObj.onlyRenaming = !result.consumeAddition;
		thisObj.repairItemCountCost = result.additionCost;
		thisObj.broadcastChanges();
		info.cancel();
	}

	@Unique
    public AnvilResult getResult() {
		AnvilMenu thisObj = (AnvilMenu) (Object) this;
		ItemStack input = thisObj.inputSlots.getItem(0);
		if (input.isEmpty()) return AnvilResult.NOOP;
		return getRenamingResult().combine(getRepairingResult());
	}

	@Unique
    public AnvilResult getRenamingResult() {
		AnvilMenu thisObj = (AnvilMenu) (Object) this;
		ItemStack input = thisObj.inputSlots.getItem(0);
		boolean isBlank = StringUtil.isBlank(thisObj.itemName);
		if (!isBlank && !thisObj.itemName.equals(input.getHoverName().getString())) {
			return AnvilResult.changeName(thisObj.itemName);
		}
		if (isBlank && input.has(DataComponents.CUSTOM_NAME)) {
			return AnvilResult.REMOVE_NAME;
		}

		return AnvilResult.NOOP;
	}

	@Unique
    public AnvilResult getRepairingResult() {
		AnvilMenu thisObj = (AnvilMenu) (Object) this;
		ItemStack input = thisObj.inputSlots.getItem(0);
		ItemStack addition = thisObj.inputSlots.getItem(1);

		if (!input.isDamaged()) return AnvilResult.NOOP;

		if (input.isValidRepairItem(addition)) {
			int repairs = Math.min(input.getDamageValue() > input.getMaxDamage() / 2 ? 2 : 1, addition.count());
			return AnvilResult.repair(repairs, repairs * input.getMaxDamage() / 2);
		}
		if (input.is(addition.getItem())) {
			int remaining = addition.getMaxDamage() - addition.getDamageValue();
			int bonus = input.getMaxDamage() * 12 / 100;
			return AnvilResult.repair(1, remaining + bonus);
		}

		return AnvilResult.FAIL;
	}

	public record AnvilResult(Function<ItemStack, ItemStack> result, int cost, boolean consumeAddition, int additionCost) {
		static AnvilResult NOOP = new AnvilResult(i -> i, 0, false, 0);
		static AnvilResult FAIL = new AnvilResult(_ -> ItemStack.EMPTY, 0, false, 0);
		static AnvilResult REMOVE_NAME = AnvilResult.consumer(i -> i.remove(DataComponents.CUSTOM_NAME), 1, false, 0);

		static AnvilResult consumer(Consumer<ItemStack> result, int cost, boolean consumeAddition, int additionCost) {
			return new AnvilResult(i -> { result.accept(i); return i; }, cost, consumeAddition, additionCost);
		}

		public AnvilResult combine(AnvilResult other) {
			return new AnvilResult(this.result.andThen(other.result), this.cost + other.cost, this.consumeAddition || other.consumeAddition, this.additionCost + other.additionCost);
		}

		static AnvilResult changeName(String name) {
			return AnvilResult.consumer(i -> i.set(DataComponents.CUSTOM_NAME, Component.literal(name)), 1, false, 0);
		}

		static AnvilResult repair(int repairs, int amount) {
			return AnvilResult.consumer(
				itemStack -> {
					itemStack.setDamageValue(Math.max(0, itemStack.getDamageValue() - amount));
					itemStack.set(DataComponents.MAX_DAMAGE, itemStack.getMaxDamage() + 1);
				},
				5 * repairs, true, repairs
			);
		}
	}
}