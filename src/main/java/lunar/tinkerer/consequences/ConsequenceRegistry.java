package lunar.tinkerer.consequences;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.ModBlocks;
import lunar.tinkerer.ModItems;
import lunar.tinkerer.consequences.effects.*;
import lunar.tinkerer.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import java.util.List;

public class ConsequenceRegistry {
    public static final Consequence DEFAULT;
    public static final Consequence CANDLE;

    static {
        DEFAULT = register("default", Consequence.EMPTY);
        CANDLE = register(
            "candle",
            new Consequence(
                Ingredient.of(Items.CANDLE),
                List.of(
                    new ApplyCurse(HolderSet.direct())
                ),
                true,
                1
            )
        );
    }

    private static Consequence register(String id, Consequence consequence) {
        return Registry.register(ModRegistries.CONSEQUENCE, MagicRevamped.identifier(id), consequence);
    }

    public static void initialize() {}
}
