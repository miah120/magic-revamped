package lunar.tinkerer.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnvilMenu.class)
public interface AnvilScreenHandlerAccessor {
    @Accessor("onlyRenaming")
    void setKeepSecondSlot(boolean keepSecondSlot);

    @Accessor("repairItemCountCost")
    void setRepairItemUsage(int repairItemUsage);

    @Accessor
    String getItemName();

    @Accessor
    DataSlot getCost();
}

