package lunar.tinkerer;

import com.mojang.serialization.Codec;
import lunar.tinkerer.consequences.ConsequenceRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
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
			.filter(
					pos -> Math.abs(pos.getX()) > 1 || Math.abs(pos.getZ()) > 1
			).map(BlockPos::immutable).toList();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModItems.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModRecipeTypes.initialize();
		ConsequenceRegistry.initialize();
		CriteriaTriggers.init();
		DataAttachments.init();
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, MagicRevamped.identifier("break_enchant"), BREAK_ENCHANT_PARTICLE);
	}

	public static Identifier identifier(String id) {
		return Identifier.fromNamespaceAndPath(MOD_ID, id);
	}

	public static class CriteriaTriggers {
		public static final EnchantItemTrigger ENCHANTED_ITEM = net.minecraft.advancements.CriteriaTriggers.register("enchant_item", new EnchantItemTrigger());

		public static void init() {}
	}

	public static class EntityTags {
		public static final TagKey<EntityType<?>> ENCHANTMENT_HELPERS = create("enchantment_helpers");

		private static TagKey<EntityType<?>> create(final String name) {
			return TagKey.create(Registries.ENTITY_TYPE, identifier(name));
		}
	}

	public static class DataAttachments {
		public static final AttachmentType<Integer> ENCHANTMENT_SKILL = AttachmentRegistry.create(
			identifier("enchantment_skill"),
			builder -> builder
				.initializer(() -> 0)
				.persistent(ExtraCodecs.NON_NEGATIVE_INT)
				.syncWith(ByteBufCodecs.INT, AttachmentSyncPredicate.targetOnly())
				.copyOnDeath()
		);

		public static void init() {}
	}
}