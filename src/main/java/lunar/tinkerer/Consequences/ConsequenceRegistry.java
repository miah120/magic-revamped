package lunar.tinkerer.Consequences;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.registry.ModRegistries;
import net.minecraft.registry.Registry;

public class ConsequenceRegistry {
    public static final Consequence OBSIDIAN = register("obsidian", new Consequence("Obsidian"));

    private static Consequence register(String id, Consequence consequence) {
        return Registry.register(ModRegistries.CONSEQUENCE, MagicRevamped.identifier(id), consequence);
    }

    public static void initialize() {}
}
