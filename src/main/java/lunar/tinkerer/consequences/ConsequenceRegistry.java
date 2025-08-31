package lunar.tinkerer.consequences;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.ModBlocks;
import lunar.tinkerer.consequences.effects.*;
import lunar.tinkerer.registry.ModRegistries;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3i;

import java.util.Collections;
import java.util.List;

public class ConsequenceRegistry {
    public static final Consequence DEFAULT;
    public static final Consequence OBSIDIAN;
    public static final Consequence CANDLE;
    public static final Consequence AMETHYST;
    public static final Consequence GLOW_BERRIES;
    public static final Consequence SOUL_LANTERN;
    public static final Consequence LIGHTNING_ROD;
    public static final Consequence COBWEB;
    public static final Consequence SKELETON_SKULL;
    public static final Consequence WITHER_SKELETON_SKULL;
    public static final Consequence GILDED_BLACKSTONE;
    public static final Consequence SEA_LANTERN;
    public static final Consequence END_ROD;
    public static final Consequence PLANT;

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
        LIGHTNING_ROD = register(
            "lightning_rod",
            new Consequence(
                "Lightning Rod",
                Ingredient.ofItem(Items.LIGHTNING_ROD),
                List.of(
                    new TransformBlock(Ingredient.ofItem(Items.LIGHTNING_ROD), Blocks.AIR.getDefaultState()),
                    new SummonLightning()
                ),
                false,
                3
            )
        );
        COBWEB = register(
            "cobweb",
            new Consequence(
                "Cobweb",
                Ingredient.ofItem(Items.COBWEB),
                List.of(
                    new TransformBlock(Ingredient.ofItem(Items.COBWEB), Blocks.AIR.getDefaultState()),
                    new SummonEntity<>(EntityType.CAVE_SPIDER, 3)
                ),
                false,
                2
            )
        );
        SKELETON_SKULL = register(
            "skeleton_skull",
            new Consequence(
                "Skeleton Skull",
                Ingredient.ofItem(Items.SKELETON_SKULL),
                List.of(
                    new EnchantSuccess(),
                    new TransformBlock(Ingredient.ofItem(Items.SKELETON_SKULL), Blocks.AIR.getDefaultState()),
                    new SummonEntity<>(EntityType.SKELETON, 3)
                ),
                true,
                1
            )
        );
        WITHER_SKELETON_SKULL = register(
            "wither_skeleton_skull",
            new Consequence(
                "Wither Skeleton Skull",
                Ingredient.ofItem(Items.WITHER_SKELETON_SKULL),
                List.of(
                    new EnchantSuccess(),
                    new TransformBlock(Ingredient.ofItem(Items.WITHER_SKELETON_SKULL), Blocks.AIR.getDefaultState()),
                    new SummonEntity<>(EntityType.WITHER_SKELETON, 3)
                ),
                true,
                1
            )
        );
        GILDED_BLACKSTONE = register(
            "gilded_blackstone",
            new Consequence(
                "",
                Ingredient.ofItem(Items.GILDED_BLACKSTONE),
                List.of(
                    new EnchantSuccess(),
                    new TransformBlock(Ingredient.ofItem(Items.GILDED_BLACKSTONE), Blocks.AIR.getDefaultState()),
                    new Explosion()
                ),
                true,
                3
            )
        );
        SEA_LANTERN = register(
            "sea_lantern",
            new Consequence(
                "Sea Lantern",
                Ingredient.ofItem(Items.SEA_LANTERN),
                List.of(
                    new TransformArea(new Vec3i(-2, -2, -2), new Vec3i(2, 2, 2), Blocks.WATER.getDefaultState(), Blocks.AIR.getDefaultState()),
                    new TransformBlock(Ingredient.ofItem(Items.SEA_LANTERN), Blocks.AIR.getDefaultState()),
                    new SummonEntity<>(EntityType.GUARDIAN, 3)
                ),
                false,
                1
            )
        );
        END_ROD = register(
            "end_rod",
            new Consequence(
                "End rod",
                Ingredient.ofItem(Items.END_ROD),
                List.of(
                    new TransformBlock(Ingredient.ofItem(Items.END_ROD), Blocks.AIR.getDefaultState()),
                    new Teleport(25, 50)
                ),
                false,
                2
            )
        );
        PLANT = register(
            "plant",
            new Consequence(
                "Plant",
                Ingredient.ofTag(Registries.createEntryLookup(Registries.ITEM).getOrThrow(ItemTags.SMALL_FLOWERS)),
                List.of(
                    new TransformBlock(Ingredient.ofTag(Registries.createEntryLookup(Registries.ITEM).getOrThrow(ItemTags.SMALL_FLOWERS)), Blocks.AIR.getDefaultState()),
                    new TransformBlock(Ingredient.ofItem(ModBlocks.ENCHANTING_TABLE.asItem()), ModBlocks.MANATHIEF.getDefaultState())
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
