package lunar.tinkerer.enchantingTable;

import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceManager;
import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.ModRecipeTypes;
import lunar.tinkerer.RuneItem;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static lunar.tinkerer.enchantingTable.ModEnchantmentScreenHandler.getLevelRequirement;

public class EnchantingResultSlot extends CraftingResultSlot {
    public final static int MAX_TIME_OUT = 100;
    public ModEnchantmentScreenHandler handler;
    public RecipeInputInventory input;
    public int timeout = 0;
    public EnchantingResultSlot(ModEnchantmentScreenHandler handler, PlayerEntity player, RecipeInputInventory input, Inventory inventory, int index, int x, int y) {
        super(player, input, inventory, index, x, y);
        this.input = input;
        this.handler = handler;
    }

    @Override
    public boolean isEnabled() {
        this.timeout = Math.max(0, this.timeout - 1);
        return super.isEnabled();
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        if(this.timeout > 0) return false;
        int levelRequirement = getLevelRequirement(this.input);
        return playerEntity.experienceLevel > levelRequirement;
    }

    public Consequence.Result<ItemStack> doConsequence(World world, BlockPos blockPos, PlayerEntity player) {
        if (!(world instanceof ServerWorld serverWorld)) return new Consequence.Result<>(ItemStack.EMPTY, false);
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return new Consequence.Result<>(ItemStack.EMPTY, false);

        //TODO: Consequence
        Consequence consequence = ConsequenceManager.pick(
            world,
            ModEnchantingTableBlock.DECORATION_OFFSETS.stream()
                .map(blockPos1 -> blockPos1.add(blockPos))
                .toList()
        );
        MagicRevamped.LOGGER.info(consequence.description());
        return consequence.run(serverWorld, blockPos, serverPlayer, this.input);
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.timeout = MAX_TIME_OUT;
        this.handler.context.run(((world, blockPos) -> {
            boolean success = this.doFluxCheck(player, input, world, blockPos);
            if (!success) {
                world.playSound(null, blockPos, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.1f + 0.9f);
                Consequence.Result<ItemStack> result = doConsequence(world, blockPos, player);
                stack.setCount(result.entry().getCount());
                stack.applyComponentsFrom(result.entry().getComponents());
                player.addExperienceLevels(-getLevelRequirement(this.input));
                if (!result.success()) return;
            };

            player.incrementStat(Stats.ENCHANT_ITEM);
            world.playSound(null, blockPos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.1f + 0.9f);
            this.onCrafted(stack);
            IntStream.range(0, this.input.size())
                 .forEach(i -> this.input.removeStack(i, 1));
        }));
        super.onTakeItem(player, stack);
        this.handler.sendContentUpdates();
    }

    private DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input, World world) {
        if (world instanceof ServerWorld serverWorld) {
            return serverWorld.getRecipeManager().getFirstMatch(ModRecipeTypes.ENCHANTMENT_RECIPE_TYPE, input, serverWorld).map(recipe -> (recipe.value()).getRecipeRemainders(input)).orElse(DefaultedList.ofSize(input.size(), ItemStack.EMPTY));
        }
        return CraftingRecipe.collectRecipeRemainders(input);
    }

    public boolean doFluxCheck(
            PlayerEntity player,
            RecipeInputInventory input,
            World world,
            BlockPos blockPos
    ) {
        int flux = ModEnchantmentScreenHandler.getFlux(input);
        int playerCheck = player.getRandom().nextBetween(0, 1000);
        int bookshelfBonus = this.getBookshelfBonus(world, blockPos);
        int bookshelfCheck = player.getRandom().nextBetween(0, bookshelfBonus);
        boolean success = (playerCheck + bookshelfCheck > flux);
        MagicRevamped.LOGGER.info(
            "{} : [{} + {}] {} {}",
            success ? "Success!" : "Failure :(",
            playerCheck,
            bookshelfCheck,
            success ? ">" : "<",
            flux
        );
        return false;
    }

    public int getSingleBookshelfBonus(World world, BlockPos blockPos) {
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if(!(blockEntity instanceof ChiseledBookshelfBlockEntity chiseledBookshelfBlockEntity)) {
            return 3;
        }
        AtomicInteger levelSum = new AtomicInteger();
        chiseledBookshelfBlockEntity.forEach(itemStack -> {
            if(itemStack.isEmpty()) return;
            int maxEnchantLevel = RuneItem.getEnchantments(itemStack)
                    .map(RuneItem.LeveledEnchantment::level)
                    .reduce(1, Integer::max);
            levelSum.addAndGet(maxEnchantLevel);
        });
        return levelSum.get();
    }

    public int getBookshelfBonus(World world, BlockPos blockPos) {
        return ModEnchantingTableBlock.POWER_PROVIDER_OFFSETS.stream()
            .map(blockPos1 -> blockPos1.add(blockPos))
            .filter(blockPos1 ->
                world.getBlockState(blockPos1)
                    .isIn(BlockTags.ENCHANTMENT_POWER_PROVIDER)
            )
            .map(blockPos1 -> this.getSingleBookshelfBonus(world, blockPos1))
            .reduce(0, Integer::sum);
    }
}
