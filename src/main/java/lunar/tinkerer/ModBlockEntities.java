package lunar.tinkerer;

import lunar.tinkerer.enchantingTable.ModEnchantmentScreenHandler;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final BlockEntityType<ManathiefBlockEntity> MANATHIEF_BLOCK_ENTITY = registerBlockEntity(
            "manathief",
            ManathiefBlockEntity::new,
            ModBlocks.MANATHIEF
    );
    public static final MenuType<ModEnchantmentScreenHandler> ENCHANTMENT_SCREEN_HANDLER = Registry.register(
            BuiltInRegistries.MENU,
            MagicRevamped.id("enchanting_table"),
            new MenuType<>(ModEnchantmentScreenHandler::new, FeatureFlagSet.of())
    );

    public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name, FabricBlockEntityTypeBuilder.Factory<T> factory, Block block) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                MagicRevamped.id(name),
                FabricBlockEntityTypeBuilder.create(factory, block).build()
        );
    }

    public static void initialize() {
        //do nothing. Calling this loads the class which in turn registers the items
    }
}
