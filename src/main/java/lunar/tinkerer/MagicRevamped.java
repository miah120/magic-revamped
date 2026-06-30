package lunar.tinkerer;

import com.google.common.base.Function;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import lunar.tinkerer.enchantingTable.ModEnchantmentScreenHandler;
import lunar.tinkerer.enchantmentRecipe.EnchantmentRecipe;
import lunar.tinkerer.enchantmentRecipe.EnchantmentRecipeDisplay;
import lunar.tinkerer.manathief.ManathiefBlock;
import lunar.tinkerer.manathief.ManathiefBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.UnaryOperator;

public class MagicRevamped implements ModInitializer {
	public static final String MOD_ID = "magic-revamped";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final SimpleParticleType BREAK_ENCHANT_PARTICLE = FabricParticleTypes.simple();
	public static List<BlockPos> DECORATION_OFFSETS = BlockPos
		.betweenClosedStream(-3, -1, -3, 3, 1, 3)
		.map(BlockPos::immutable).toList();
	public static List<BlockPos> POWER_PROVIDER_OFFSETS = BlockPos
		.betweenClosedStream(-3, 0, -3, 3, 1, 3)
		.filter(pos -> Math.abs(pos.getX()) > 1 || Math.abs(pos.getZ()) > 1)
		.map(BlockPos::immutable).toList();
	public static Identifier ENCHANTING_RECIPE_TYPE = MagicRevamped.id("enchanting");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Items.init();
		Blocks.init();
		BlockEntities.init();
		ScreenHandlers.init();
		RecipeTypes.init();
		Consequence.init();
		CriteriaTriggers.init();
		DataAttachments.init();
		DataComponents.init();
		Registries.init();
		RecipeSerializers.init();
		RecipeBookCategories.init();
		RecipeDisplayTypes.init();
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, MagicRevamped.id("break_enchant"), BREAK_ENCHANT_PARTICLE);
	}

	public static Identifier id(String id) {
		return Identifier.fromNamespaceAndPath(MOD_ID, id);
	}

	public static class CriteriaTriggers {
		public static final EnchantItemTrigger ENCHANTED_ITEM = Registry.register(BuiltInRegistries.TRIGGER_TYPES, "enchant_item", new EnchantItemTrigger());

		public static void init() {}
	}

	public static class EntityTags {
		public static final TagKey<EntityType<?>> ENCHANTMENT_HELPERS = TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, id("enchantment_helpers"));
	}

	public static class DataAttachments {
		public static final AttachmentType<Integer> ENCHANTMENT_SKILL = AttachmentRegistry.create(
			id("enchantment_skill"),
			builder -> builder
				.initializer(() -> 0)
				.persistent(ExtraCodecs.NON_NEGATIVE_INT)
				.syncWith(ByteBufCodecs.INT, AttachmentSyncPredicate.targetOnly())
				.copyOnDeath()
		);

		public static void init() {}
	}

	public static class DataComponents {
		public static DataComponentType<RuneItem.Flux> FLUX = register(
			"flux",
			builder -> builder.persistent(RuneItem.Flux.CODEC).networkSynchronized(RuneItem.Flux.STREAM_CODEC)
		);

		public static DataComponentType<Unit> OPEN = register(
			"open",
			builder -> builder.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC)
		);

		public static DataComponentType<Unit> CHARGED = register(
			"charged",
			builder -> builder.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC)
		);

		public static DataComponentType<Holder<Enchantment>> ENCHANTMENT = register(
			"enchantment",
			builder -> builder.persistent(Enchantment.CODEC).networkSynchronized(Enchantment.STREAM_CODEC)
		);

		public static DataComponentType<Boolean> HAS_BOOK = register(
			"has_book",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
		);

		private static <T> DataComponentType<T> register(final String id, final UnaryOperator<DataComponentType.Builder<T>> builder) {
			return net.minecraft.core.component.DataComponents.register(MagicRevamped.id(id).toString(), builder);
		}

		public static void init() {
			ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, list) -> {
				if (!itemStack.is(Items.RUNE)) return;
				itemStack.getOrDefault(FLUX, RuneItem.DEFAULT_RUNE_FLUX)
					.addToTooltip(tooltipContext, x -> list.add(1, x), tooltipType, itemStack.getComponents());
			});
		}
	}

	public static class Items {
		public static final Item RUNE = net.minecraft.world.item.Items.registerItem(
			ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, MagicRevamped.id("rune")),
			RuneItem::new,
			new Item.Properties()
				.component(DataComponents.ENCHANTMENT, null)
				.component(DataComponents.FLUX, RuneItem.DEFAULT_RUNE_FLUX)
		);

		public static final TagKey<Item> DROPS_CHARGED_RUNE = TagKey.create(net.minecraft.core.registries.Registries.ITEM, MagicRevamped.id("drops_charged_rune"));

		public static void init() {
			addBlocksToItemGroup();
		}

		public static void addBlocksToItemGroup() {
			CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(content ->
				content.getContext().holders().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT).ifPresent(registryWrapper -> {
					RuneItem.addOpenRunes(content, registryWrapper, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
					RuneItem.addOpenAndClosedRunes(content, registryWrapper, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
				})
			);
			CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(content ->
				content.getContext().holders().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT).ifPresent(registryWrapper -> {
					RuneItem.addOpenRunes(content, registryWrapper, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
					RuneItem.addOpenAndClosedRunes(content, registryWrapper, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
				})
			);
		}
	}

	public static class Blocks {
		public static final Block MANATHIEF = register(
			"manathief",
			ManathiefBlock::new,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLACK)
				.lightLevel(_ -> 7)
				.noCollision()
				.instabreak()
				.sound(SoundType.CROP)
		);

		public static void init() {}

		private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings) {
			BlockItemId id = BlockItemId.create(id(name), id(name));
			Block block = net.minecraft.world.level.block.Blocks.register(id.block(), blockFactory, settings);
			net.minecraft.world.item.Items.registerBlock(id, block);
            return block;
		}
	}

	public static class RecipeSerializers {
		public static final RecipeSerializer<EnchantmentRecipe> ENCHANTMENT_RECIPE_SERIALIZER = Registry.register(
			BuiltInRegistries.RECIPE_SERIALIZER,
			ENCHANTING_RECIPE_TYPE.toString(),
			EnchantmentRecipe.SERIALIZER
		);

		public static void init() {}
	}

	public static class RecipeTypes {
		public static final RecipeType<EnchantmentRecipe> ENCHANTMENT_RECIPE_TYPE =  Registry.register(
			BuiltInRegistries.RECIPE_TYPE,
			ENCHANTING_RECIPE_TYPE,
			new RecipeType<EnchantmentRecipe>(){
				@Override
				public String toString() {
					return ENCHANTING_RECIPE_TYPE.toString();
				}
			}
		);

		public static void init() {}
	}

	public static class RecipeBookCategories {
		public static final RecipeBookCategory ENCHANTMENT_RECIPE_BOOK_CATEGORY = Registry.register(
			BuiltInRegistries.RECIPE_BOOK_CATEGORY,
				ENCHANTING_RECIPE_TYPE,
				new RecipeBookCategory()
		);

		public static void init() {}
	}

	public static class RecipeDisplayTypes {
		public static final RecipeDisplay.Type<EnchantmentRecipeDisplay> ENCHANTMENT_RECIPE_DISPLAY = Registry.register(
			BuiltInRegistries.RECIPE_DISPLAY,
			ENCHANTING_RECIPE_TYPE,
			EnchantmentRecipeDisplay.SERIALIZER
		);

		public static void init() {}
	}

	public static class BlockEntities {
		public static final BlockEntityType<ManathiefBlockEntity> MANATHIEF_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			id("manathief"),
			FabricBlockEntityTypeBuilder.create(ManathiefBlockEntity::new, Blocks.MANATHIEF).build()
		);

		public static void init() {}
	}

	public static class ScreenHandlers {
		public static final MenuType<ModEnchantmentScreenHandler> ENCHANTMENT_SCREEN_HANDLER = Registry.register(
			BuiltInRegistries.MENU,
			id("enchanting_table"),
			new MenuType<>(ModEnchantmentScreenHandler::new, FeatureFlagSet.of())
		);

		public static void init() {}
	}

	public static class Registries {
		public static final Registry<MapCodec<? extends ConsequenceEffect>> CONSEQUENCE_EFFECT = create(RegistryKeys.CONSEQUENCE_EFFECT, ConsequenceEffect::bootstrap);

		public static void init() {
			DynamicRegistries.registerSynced(RegistryKeys.CONSEQUENCE, Consequence.CODEC);
		}

		private static <T> Registry<T> create(ResourceKey<Registry<T>> registryKey, java.util.function.Function<Registry<T>, Object> loader) {
			Registry<T> registry = FabricRegistryBuilder.create(registryKey).buildAndRegister();
			loader.apply(registry);
			return registry;
		}
	}

	public static class RegistryKeys {
		public static final ResourceKey<Registry<Consequence>> CONSEQUENCE = create("consequence");
		public static final ResourceKey<Registry<MapCodec<? extends ConsequenceEffect>>> CONSEQUENCE_EFFECT = create("consequence_effect");

		private static <T> ResourceKey<Registry<T>> create(String path) {
			return ResourceKey.createRegistryKey(MagicRevamped.id(path));
		}
	}
}