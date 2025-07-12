package lunar.tinkerer.Consequences;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.registry.ModRegistries;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registry;

public class ConsequenceRegistry {
    public static final Consequence OBSIDIAN;
    public static final Consequence DEFAULT;

    static {
        DEFAULT = register("default", Consequence.EMPTY);
        OBSIDIAN = register(
            "obsidian",
            new Consequence("Obsidian", Ingredient.ofItem(Items.OBSIDIAN), 2)
        );
    }


    private static Consequence register(String id, Consequence consequence) {
        return Registry.register(ModRegistries.CONSEQUENCE, MagicRevamped.identifier(id), consequence);
    }

    public static void initialize() {}
}
