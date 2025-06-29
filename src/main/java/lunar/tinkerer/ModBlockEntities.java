package lunar.tinkerer;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<ModEnchantingTableBlockEntity> ENCHANTING_TABLE_BLOCK_ENTITY = registerBlockEntity(
            "enchanting_table",
            ModEnchantingTableBlockEntity::new,
            ModBlocks.ENCHANTING_TABLE
    );
    public static final ScreenHandlerType<ModEnchantmentScreenHandler> ENCHANTMENT_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MagicRevamped.MOD_ID, "enchanting_table"), new ScreenHandlerType<>(ModEnchantmentScreenHandler::new, FeatureSet.empty()));
    public static final RecipeType<CraftingRecipe> ENCHANTMENT_RECIPE = registerRecipeType("enchanting");

    public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name, FabricBlockEntityTypeBuilder.Factory<T> factory, Block block) {
        return Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(MagicRevamped.MOD_ID, name),
                FabricBlockEntityTypeBuilder.create(factory, block).build()
        );
    }

    public static void initialize() {
        //do nothing. Calling this loads the class which in turn registers the items
    }

    public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(final String id) {
        return Registry.register(Registries.RECIPE_TYPE, Identifier.of(MagicRevamped.MOD_ID, id), new RecipeType<T>(){
            public String toString() {
                return id;
            }
        });
    }

}
