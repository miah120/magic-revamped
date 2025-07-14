package lunar.tinkerer.consequences;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.consequences.effects.PlaySound;
import lunar.tinkerer.consequences.effects.TransformBlock;
import lunar.tinkerer.registry.ModRegistries;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;

import java.util.List;

public class ConsequenceRegistry {
    public static final Consequence OBSIDIAN;
    public static final Consequence DEFAULT;

    static {
        DEFAULT = register("default", Consequence.EMPTY);
        OBSIDIAN = register(
            "obsidian",
            new Consequence(
                "Obsidian",
                Ingredient.ofItem(Items.OBSIDIAN),
                List.of(
                    new PlaySound(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value()),
                    new TransformBlock(Ingredient.ofItem(Items.OBSIDIAN), Blocks.CRYING_OBSIDIAN.getDefaultState())
                ),
                false,
                2
            )
        );
    }


    private static Consequence register(String id, Consequence consequence) {
        return Registry.register(ModRegistries.CONSEQUENCE, MagicRevamped.identifier(id), consequence);
    }

    public static void initialize() {}
}
