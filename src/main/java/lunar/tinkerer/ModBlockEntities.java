package lunar.tinkerer;

import lunar.tinkerer.enchantingTable.ModEnchantingTableBlockEntity;
import lunar.tinkerer.enchantingTable.ModEnchantmentScreenHandler;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;

public class ModBlockEntities {
    public static final BlockEntityType<ModEnchantingTableBlockEntity> ENCHANTING_TABLE_BLOCK_ENTITY = registerBlockEntity(
            "enchanting_table",
            ModEnchantingTableBlockEntity::new,
            ModBlocks.ENCHANTING_TABLE
    );
    public static final BlockEntityType<ManathiefBlockEntity> MANATHIEF_BLOCK_ENTITY = registerBlockEntity(
            "manathief",
            ManathiefBlockEntity::new,
            ModBlocks.MANATHIEF
    );
    public static final ScreenHandlerType<ModEnchantmentScreenHandler> ENCHANTMENT_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            MagicRevamped.identifier("enchanting_table"),
            new ScreenHandlerType<>(ModEnchantmentScreenHandler::new, FeatureSet.empty())
    );

    public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name, FabricBlockEntityTypeBuilder.Factory<T> factory, Block block) {
        return Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                MagicRevamped.identifier(name),
                FabricBlockEntityTypeBuilder.create(factory, block).build()
        );
    }

    public static void initialize() {
        //do nothing. Calling this loads the class which in turn registers the items
    }
}
