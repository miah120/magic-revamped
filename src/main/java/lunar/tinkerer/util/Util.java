package lunar.tinkerer.util;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class Util {
    public static void drop(Level level, Vec3 position, ItemStack itemStack) {
        Supplier<Float> r = () -> level.getRandom().nextFloat();
        Vec3 location = position.add((r.get() - 0.5) * 0.1F, r.get() * 0.05F, (r.get() - 0.5) * 0.1F);
        ItemEntity entity = new ItemEntity(level, location.x, location.y, location.z, itemStack);
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);
    }
}
