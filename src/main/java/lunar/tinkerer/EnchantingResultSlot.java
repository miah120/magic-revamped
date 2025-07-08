package lunar.tinkerer;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.entry.RegistryEntry;
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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static lunar.tinkerer.ModEnchantmentScreenHandler.getLevelRequirement;

public class EnchantingResultSlot extends CraftingResultSlot {
    public ModEnchantmentScreenHandler handler;
    public RecipeInputInventory input;
    public EnchantingResultSlot(ModEnchantmentScreenHandler handler, PlayerEntity player, RecipeInputInventory input, Inventory inventory, int index, int x, int y) {
        super(player, input, inventory, index, x, y);
        this.input = input;
        this.handler = handler;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        int levelRequirement = getLevelRequirement(this.input);
        return playerEntity.experienceLevel > levelRequirement;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.handler.context.run(((world, blockPos) -> {
            this.handler.seed.set(player.getEnchantingTableSeed());
            Result<ItemStack> result = this.doFluxCheck(player, stack, input, world, blockPos);
            if (!result.success) {
                stack.setCount(0);
                player.addExperienceLevels(-getLevelRequirement(this.input));
                //TODO: Consequence
                if (player instanceof ServerPlayerEntity serverPlayerEntity) {
                    serverPlayerEntity.closeHandledScreen();
                }
                return;
            };

            player.incrementStat(Stats.ENCHANT_ITEM);
            world.playSound(null, blockPos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.1f + 0.9f);
            this.onCrafted(result.entry);
            CraftingRecipeInput.Positioned positioned = this.input.createPositionedRecipeInput();
            CraftingRecipeInput craftingRecipeInput = positioned.input();
            int i = positioned.left();
            int j = positioned.top();
            DefaultedList<ItemStack> defaultedList = this.getRecipeRemainders(craftingRecipeInput, player.getWorld());

            //TODO: Clean this up
            for (int k = 0; k < craftingRecipeInput.getHeight(); ++k) {
                for (int l = 0; l < craftingRecipeInput.getWidth(); ++l) {
                    int m = l + i + (k + j) * this.input.getWidth();
                    ItemStack itemStack = this.input.getStack(m);
                    ItemStack itemStack2 = defaultedList.get(l + k * craftingRecipeInput.getWidth());
                    if (!itemStack.isEmpty()) {
                        this.input.removeStack(m, 1);
                        itemStack = this.input.getStack(m);
                    }
                    if (itemStack2.isEmpty()) continue;
                    if (itemStack.isEmpty()) {
                        this.input.setStack(m, itemStack2);
                        continue;
                    }
                    if (ItemStack.areItemsAndComponentsEqual(itemStack, itemStack2)) {
                        itemStack2.increment(itemStack.getCount());
                        this.input.setStack(m, itemStack2);
                        continue;
                    }
                    if (player.getInventory().insertStack(itemStack2)) continue;
                    player.dropItem(itemStack2, false);
                }
            }
        }));
    }

    private DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input, World world) {
        if (world instanceof ServerWorld serverWorld) {
            return serverWorld.getRecipeManager().getFirstMatch(ModRecipeTypes.ENCHANTMENT_RECIPE_TYPE, input, serverWorld).map(recipe -> (recipe.value()).getRecipeRemainders(input)).orElse(DefaultedList.ofSize(input.size(), ItemStack.EMPTY));
        }
        return CraftingRecipe.collectRecipeRemainders(input);
    }

    public record Result<T> (T entry, boolean success) {}

    public Result<ItemStack> doFluxCheck(
            PlayerEntity player,
            ItemStack stack,
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
            "Success:{} = [{} + {}] > {}",
            success,
            playerCheck,
            bookshelfCheck,
            flux
        );
        return new Result<>(stack, true);
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
        var e = ModEnchantingTableBlock.POWER_PROVIDER_OFFSETS.stream()
            .map(blockPos1 -> blockPos1.add(blockPos))
            .filter(blockPos1 ->
                world.getBlockState(blockPos1)
                    .isIn(BlockTags.ENCHANTMENT_POWER_PROVIDER)
            )
            .map(blockPos1 -> this.getSingleBookshelfBonus(world, blockPos1))
            .reduce(0, Integer::sum);
        MagicRevamped.LOGGER.info(e.toString());
        return e;
    }
}
