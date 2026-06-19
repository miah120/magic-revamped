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
                Ingredient.of(Items.OBSIDIAN),
                List.of(
                    new PlaySound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value()),
                    new TransformBlock(Ingredient.of(Items.OBSIDIAN), Blocks.CRYING_OBSIDIAN.defaultBlockState())
                ),
                false,
                2
            )
        );
        CANDLE = register(
            "candle",
            new Consequence(
                Ingredient.of(Items.CANDLE),
                List.of(
                    new ApplyCurse(HolderSet.direct()),
                    new PlaySound(SoundEvents.CANDLE_EXTINGUISH),
                    new TransformBlock(Ingredient.of(Items.CANDLE), Blocks.AIR.defaultBlockState())
                ),
                true,
                1
            )
        );
        AMETHYST = register(
            "amethyst",
            new Consequence(
                Ingredient.of(Items.AMETHYST_CLUSTER, Items.SMALL_AMETHYST_BUD, Items.MEDIUM_AMETHYST_BUD, Items.LARGE_AMETHYST_BUD),
                List.of(
                    new PlaySound(SoundEvents.AMETHYST_CLUSTER_BREAK),
                    new TransformBlock(Ingredient.of(Items.AMETHYST_CLUSTER), Blocks.AIR.defaultBlockState()),
                    new TransformBlock(Ingredient.of(Items.BOOKSHELF, Items.CHISELED_BOOKSHELF), Blocks.AMETHYST_BLOCK.defaultBlockState())
                ),
                false,
                2
            )
        );
        GLOW_BERRIES = register(
            "glow_berries",
            new Consequence(
                Ingredient.of(Items.GLOW_BERRIES),
                List.of(
                    new PlaySound(SoundEvents.BONE_MEAL_USE),
                    new TransformBlock(Ingredient.of(Items.GLOW_BERRIES), Blocks.AIR.defaultBlockState()),
                    new TransformBlock(Ingredient.of(Items.BOOKSHELF, Items.CHISELED_BOOKSHELF), Blocks.MOSS_BLOCK.defaultBlockState())
                ),
                false,
                1
            )
        );
        SOUL_LANTERN = register(
            "soul_lantern",
            new Consequence(
                Ingredient.of(Items.SOUL_LANTERN, Items.SOUL_CAMPFIRE, Items.SOUL_TORCH),
                List.of(
                    new PlaySound(SoundEvents.SOUL_ESCAPE.value()),
                    new PlaySound(SoundEvents.LANTERN_BREAK),
                    new TransformBlock(Ingredient.of(Items.SOUL_LANTERN, Items.SOUL_CAMPFIRE, Items.SOUL_TORCH), Blocks.AIR.defaultBlockState()),
                    new ApplyEffect(MobEffects.WITHER)
                ),
                false,
                1
            )
        );
        LIGHTNING_ROD = register(
            "lightning_rod",
            new Consequence(
                Ingredient.of(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM).getOrThrow(ModItems.SUMMONS_LIGHTNING)),
                List.of(
                    new TransformBlock(Ingredient.of(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM).getOrThrow(ModItems.SUMMONS_LIGHTNING)), Blocks.AIR.defaultBlockState()),
                    new SummonLightning()
                ),
                false,
                3
            )
        );
        COBWEB = register(
            "cobweb",
            new Consequence(
                Ingredient.of(Items.COBWEB),
                List.of(
                    new TransformBlock(Ingredient.of(Items.COBWEB), Blocks.AIR.defaultBlockState()),
                    new SummonEntity<>(EntityType.CAVE_SPIDER, 3)
                ),
                false,
                2
            )
        );
        SKELETON_SKULL = register(
            "skeleton_skull",
            new Consequence(
                Ingredient.of(Items.SKELETON_SKULL),
                List.of(
                    new EnchantSuccess(),
                    new TransformBlock(Ingredient.of(Items.SKELETON_SKULL), Blocks.AIR.defaultBlockState()),
                    new SummonEntity<>(EntityType.SKELETON, 3)
                ),
                true,
                1
            )
        );
        WITHER_SKELETON_SKULL = register(
            "wither_skeleton_skull",
            new Consequence(
                Ingredient.of(Items.WITHER_SKELETON_SKULL),
                List.of(
                    new EnchantSuccess(),
                    new TransformBlock(Ingredient.of(Items.WITHER_SKELETON_SKULL), Blocks.AIR.defaultBlockState()),
                    new SummonEntity<>(EntityType.WITHER_SKELETON, 3)
                ),
                true,
                1
            )
        );
        GILDED_BLACKSTONE = register(
            "gilded_blackstone",
            new Consequence(
                Ingredient.of(Items.GILDED_BLACKSTONE),
                List.of(
                    new EnchantSuccess(),
                    new TransformBlock(Ingredient.of(Items.GILDED_BLACKSTONE), Blocks.AIR.defaultBlockState()),
                    new Explosion()
                ),
                true,
                3
            )
        );
        SEA_LANTERN = register(
            "sea_lantern",
            new Consequence(
                Ingredient.of(Items.SEA_LANTERN),
                List.of(
                    new TransformArea(new Vec3i(-2, -2, -2), new Vec3i(2, 2, 2), Blocks.WATER.defaultBlockState(), Blocks.AIR.defaultBlockState()),
                    new TransformBlock(Ingredient.of(Items.SEA_LANTERN), Blocks.AIR.defaultBlockState()),
                    new SummonEntity<>(EntityType.GUARDIAN, 3)
                ),
                false,
                1
            )
        );
        END_ROD = register(
            "end_rod",
            new Consequence(
                Ingredient.of(Items.END_ROD),
                List.of(
                    new TransformBlock(Ingredient.of(Items.END_ROD), Blocks.AIR.defaultBlockState()),
                    new Teleport(25, 50)
                ),
                false,
                2
            )
        );
        PLANT = register(
            "plant",
            new Consequence(
                Ingredient.of(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM).getOrThrow(ItemTags.SMALL_FLOWERS)),
                List.of(
                    new TransformBlock(Ingredient.of(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM).getOrThrow(ItemTags.SMALL_FLOWERS)), Blocks.AIR.defaultBlockState()),
                    new TransformBlock(Ingredient.of(Blocks.ENCHANTING_TABLE.asItem()), ModBlocks.MANATHIEF.defaultBlockState())
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
