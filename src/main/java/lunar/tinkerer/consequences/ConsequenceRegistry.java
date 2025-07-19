package lunar.tinkerer.consequences;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.consequences.effects.ApplyCurse;
import lunar.tinkerer.consequences.effects.ApplyEffect;
import lunar.tinkerer.consequences.effects.PlaySound;
import lunar.tinkerer.consequences.effects.TransformBlock;
import lunar.tinkerer.registry.ModRegistries;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;

import java.util.List;

public class ConsequenceRegistry {
    public static final Consequence DEFAULT;
    public static final Consequence OBSIDIAN;
    public static final Consequence CANDLE;
    public static final Consequence AMETHYST;
    public static final Consequence GLOW_BERRIES;
    public static final Consequence SOUL_LANTERN;

    //TODO: Implement the rest of the Consequences
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
        CANDLE = register(
            "candle",
            new Consequence(
                "Candle",
                Ingredient.ofItem(Items.CANDLE),
                List.of(
                    new ApplyCurse(),
                    new PlaySound(SoundEvents.BLOCK_CANDLE_EXTINGUISH),
                    new TransformBlock(Ingredient.ofItem(Items.CANDLE), Blocks.AIR.getDefaultState())
                ),
                true,
                1
            )
        );
        AMETHYST = register(
            "amethyst",
            new Consequence(
                "Amethyst",
                Ingredient.ofItems(Items.AMETHYST_CLUSTER, Items.SMALL_AMETHYST_BUD, Items.MEDIUM_AMETHYST_BUD, Items.LARGE_AMETHYST_BUD),
                List.of(
                    new PlaySound(SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK),
                    new TransformBlock(Ingredient.ofItem(Items.AMETHYST_CLUSTER), Blocks.AIR.getDefaultState()),
                    new TransformBlock(Ingredient.ofItems(Items.BOOKSHELF, Items.CHISELED_BOOKSHELF), Blocks.AMETHYST_BLOCK.getDefaultState())
                ),
                false,
                2
            )
        );
        GLOW_BERRIES = register(
            "glow_berries",
            new Consequence(
                "Glow Berries",
                Ingredient.ofItem(Items.GLOW_BERRIES),
                List.of(
                    new PlaySound(SoundEvents.ITEM_BONE_MEAL_USE),
                    new TransformBlock(Ingredient.ofItem(Items.GLOW_BERRIES), Blocks.AIR.getDefaultState()),
                    new TransformBlock(Ingredient.ofItems(Items.BOOKSHELF, Items.CHISELED_BOOKSHELF), Blocks.MOSS_BLOCK.getDefaultState())
                ),
                false,
                1
            )
        );
        SOUL_LANTERN = register(
            "soul_lantern",
            new Consequence(
                "Soul Lantern",
                Ingredient.ofItems(Items.SOUL_LANTERN, Items.SOUL_CAMPFIRE, Items.SOUL_TORCH),
                List.of(
                    new PlaySound(SoundEvents.PARTICLE_SOUL_ESCAPE.value()),
                    new PlaySound(SoundEvents.BLOCK_LANTERN_BREAK),
                    new TransformBlock(Ingredient.ofItems(Items.SOUL_LANTERN, Items.SOUL_CAMPFIRE, Items.SOUL_TORCH), Blocks.AIR.getDefaultState()),
                    new ApplyEffect(StatusEffects.WITHER)
                ),
                false,
                1
            )
        );
    }


    private static Consequence register(String id, Consequence consequence) {
        return Registry.register(ModRegistries.CONSEQUENCE, MagicRevamped.identifier(id), consequence);
    }

    public static void initialize() {}
}
