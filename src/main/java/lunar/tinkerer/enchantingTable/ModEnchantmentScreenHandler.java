package lunar.tinkerer.enchantingTable;

import lunar.tinkerer.MagicRevamped;
import lunar.tinkerer.RuneItem;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.enchantmentRecipe.EnchantmentRecipe;
import lunar.tinkerer.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ModEnchantmentScreenHandler
        extends RecipeBookMenu {
    public final ContainerLevelAccess context;
    public final Player player;
    private boolean filling;
    public final static int MAX_TIME_OUT = 20;
    public final DataSlot timeout = DataSlot.standalone();
    public final DataSlot seed = DataSlot.standalone();
    public final EnchantmentCraftingContainer craftingInventory;
    protected final EnchantingTableResultInventory craftingResultInventory = new EnchantingTableResultInventory();
    public EnchantingResultSlot resultSlot;
    public static final Map<Integer, Integer> BOOKSHELF_BONUSES = Map.of(
        1, 3,
        2, 5,
        3, 8,
        4, 13,
        5, 21,
        6, 34
    );

    public ModEnchantmentScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ModEnchantmentScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(MagicRevamped.ScreenHandlers.ENCHANTMENT_SCREEN_HANDLER, syncId);
        this.addDataSlot(this.seed).set(playerInventory.player.getEnchantmentSeed());
        this.addDataSlot(this.timeout).set(0);
        this.context = context;
        this.player = playerInventory.player;
        this.craftingInventory = new EnchantmentCraftingContainer(this);
        this.resultSlot = new EnchantingResultSlot(
            this,
            player,
            this.craftingInventory,
            this.craftingResultInventory,
            0,
            127,
            24
        );
        this.addResultSlot();
        this.addInputSlots();
        this.addStandardInventorySlots(playerInventory, 8, 91);

        context.execute((world, _) -> Objects.requireNonNull(world.getServer()).addTickable(this::tickTimeout));
    }

    public BlockPos getBlockPos() {
        return this.context.evaluate((_, pos) -> pos, this.player.blockPosition());
    }

    protected void addResultSlot() {
        this.addSlot(this.resultSlot);
    }

    protected void addInputSlots() {
        int offset = 30;
        int corner = 24;
        Stream.of(
                new Vec3i(0, 0, 0),
                new Vec3i(1, 0, -offset),
                new Vec3i(2, corner, -corner),
                new Vec3i(3, offset, 0),
                new Vec3i(4, corner, corner),
                new Vec3i(5, 0, offset),
                new Vec3i(6, -corner, corner),
                new Vec3i(7, -offset, 0),
                new Vec3i(8, -corner, -corner)
            )
            .map(info -> info.offset(0, 44 ,24))
            .map(info -> new Slot(this.craftingInventory, info.getX(), info.getY(), info.getZ()))
            .forEach(this::addSlot);
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        this.context.execute((_, _) -> this.clearContainer(player, this.craftingInventory));
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        var hasBook = this.context.evaluate((level, blockPos) -> level.getBlockState(blockPos).getValue(BlockStateProperties.HAS_BOOK), true);
        return hasBook && ModEnchantmentScreenHandler.stillValid(this.context, player, Blocks.ENCHANTING_TABLE);
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slot) {
        Slot sourceSlot = this.slots.get(slot);
        ItemStack original = sourceSlot.getItem().copy();
        ItemStack itemStack = switch (slot) {
            case 0 -> quickMoveFromResult(player);
            case 1,2,3,4,5,6,7,8,9 -> quickMoveFromTable(slot);
            default -> quickMoveFromPlayer(slot);
        };

        sourceSlot.setChanged();

        if (original.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
        }

        if (slot == 0) {
            player.drop(itemStack, false);
        } else {
            //onTakeItem handled by quickMoveFromResult
            sourceSlot.onTake(player, itemStack);
        }
        this.broadcastChanges();
        return itemStack;
    }

    private ItemStack quickMoveFromResult(Player player) {
        if(!this.resultSlot.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack items = this.resultSlot.getItem();
        if(items.isEmpty()) return ItemStack.EMPTY;
        this.resultSlot.onTake(player, items);
        items.getItem().onCraftedBy(items, player);
        this.moveItemStackTo(items, 10, 46, true);
        this.slotsChanged(this.craftingInventory);
        return items;
    }

    private ItemStack quickMoveFromTable(int slot) {
        ItemStack items = this.slots.get(slot).getItem();
        this.moveItemStackTo(items, 10, 46, false);
        return items;
    }

    private ItemStack quickMoveFromPlayer(int slot) {
        ItemStack items = this.slots.get(slot).getItem();
        if(this.moveItemStackTo(items, 1, 10, false)) {
            return items;
        }

        if (slot < 37) {
            this.moveItemStackTo(items, 37, 46, false);
        } else {
            this.moveItemStackTo(items, 10, 37, false);
        }
        return items;
    }

    @Override
    public @NonNull PostPlaceAction handlePlacement(boolean craftAll, boolean creative, @NonNull RecipeHolder<?> recipe, @NonNull ServerLevel world, @NonNull Inventory inventory) {
        if (!(recipe.value() instanceof EnchantmentRecipe recipeEntryValue)) return PostPlaceAction.NOTHING;
        RecipeHolder<EnchantmentRecipe> recipeEntry = new RecipeHolder<>(recipe.id(), recipeEntryValue);
        this.onInputSlotFillStart();
        try {
            List<Slot> list = this.getInputSlots();
            return ServerPlaceRecipe.placeRecipe(
                new PlaceEnchantmentRecipe(this),
                3, 3, list, list, inventory, recipeEntry, craftAll, creative
            );
        } finally {
            this.onInputSlotFillFinish(world, recipeEntry);
        }
    }

    @Override
    public void fillCraftSlotsStackedContents(@NonNull StackedItemContents finder) {
        this.craftingInventory.fillStackedContents(finder);
    }

    protected static void updateResult(ModEnchantmentScreenHandler handler, ServerLevel world, Player player, EnchantmentCraftingContainer craftingInventory, EnchantingTableResultInventory resultInventory, @Nullable RecipeHolder<EnchantmentRecipe> recipe) {
        if (!(player instanceof ServerPlayer serverPlayerEntity)) return;
        ItemStack itemStack = getResult(world, player, craftingInventory, resultInventory, recipe);
        resultInventory.setItem(0, itemStack);
        handler.setRemoteSlot(0, itemStack);
        serverPlayerEntity.connection.send(new ClientboundContainerSetSlotPacket(handler.containerId, handler.incrementStateId(), 0, itemStack));
    }

    public static ItemStack getResult(ServerLevel world, Player player, EnchantmentCraftingContainer craftingInventory, EnchantingTableResultInventory resultInventory, @Nullable RecipeHolder<EnchantmentRecipe> recipe) {
        ItemStack conduit = craftingInventory.getConduit();
        if (conduit.isEmpty()) {
            return ItemStack.EMPTY;
        } else if (conduit.is(Items.LAPIS_LAZULI)) {
            return carveRune(world, player, craftingInventory, resultInventory, recipe);
        } else if (conduit.is(MagicRevamped.Items.RUNE)) {
            return stabilize(craftingInventory);
        } else {
            return enchant(craftingInventory.getConduit(), craftingInventory);
        }
    }

    private static ItemStack stabilize(EnchantmentCraftingContainer craftingInventory) {
        List<ItemStack> additions = craftingInventory.getNonEmptyAdditions().toList();
        if (additions.stream().anyMatch(itemStack -> !itemStack.is(Items.DIAMOND))) return ItemStack.EMPTY;

        int stabilization = (int) additions.stream()
            .filter(itemStack -> itemStack.is(Items.DIAMOND))
            .count();

        ItemStack conduit = craftingInventory.getConduit();
        return RuneItem.changeFlux(conduit, flux -> flux - stabilization)
            .map(flux -> {
                ItemStack result = conduit.copyWithCount(1);
                result.set(MagicRevamped.DataComponents.FLUX, flux);
                return result;
            })
            .orElse(ItemStack.EMPTY);
    }

    private static ItemStack carveRune(ServerLevel world, Player player, EnchantmentCraftingContainer craftingInventory, EnchantingTableResultInventory resultInventory, @Nullable RecipeHolder<EnchantmentRecipe> recipeHolder) {
        if (!(player instanceof ServerPlayer serverPlayerEntity)) return ItemStack.EMPTY;
        CraftingInput craftingRecipeInput = craftingInventory.asCraftInput();
        return Optional.ofNullable(recipeHolder)
            .flatMap(recipe -> world.getServer().getRecipeManager().getRecipeFor(
                MagicRevamped.RecipeTypes.ENCHANTMENT_RECIPE_TYPE,
                craftingRecipeInput,
                world,
                recipe.id()
            ))
            .filter(recipeEntry -> resultInventory.setRecipeUsed(serverPlayerEntity, recipeEntry))
            .map(recipeEntry -> recipeEntry.value().assemble(craftingRecipeInput))
            .filter(itemStack1 -> itemStack1.isItemEnabled(world.enabledFeatures()))
            .orElse(ItemStack.EMPTY);
    }

    public static ItemStack enchant(ItemStack conduit, EnchantmentCraftingContainer craftingInventory) {
        if (!EnchantmentHelper.canStoreEnchantments(conduit) && !conduit.is(Items.BOOK)) return ItemStack.EMPTY;
        if (craftingInventory.getNonEmptyAdditions().anyMatch(i -> !isValidRuneForItem(i, conduit))) return ItemStack.EMPTY;

        ItemStack result = craftingInventory.getNonEmptyAdditions()
            .sorted(Comparator.comparingInt(RuneItem::order))
            .reduce(
                conduit.is(Items.BOOK) ? new ItemStack(Items.ENCHANTED_BOOK) : conduit.copy(),
                ModEnchantmentScreenHandler::updateValidEnchantments
            );

        if (result.isDamageableItem()) {
            result.set(DataComponents.MAX_DAMAGE, Math.max(result.getMaxDamage() - 1, 1));
        }

        return result;
    }

    public static ItemStack updateValidEnchantments(ItemStack subResult, ItemStack rune) {
        RuneItem.getEnchantment(rune)
            .filter(_ -> isValidRuneForItem(rune, subResult))
            .ifPresent(entry -> {
                int nextLevel = getResultEnchantmentLevel(subResult, entry, entry.value(), rune);
                EnchantmentHelper.updateEnchantments(subResult, builder -> builder.upgrade(entry, nextLevel));
            });
        return subResult;
    }

    public static boolean isValidRuneForItem(ItemStack rune, ItemStack conduit) {
        if (!rune.is(MagicRevamped.Items.RUNE)) return false;
        boolean isBook = conduit.is(Items.ENCHANTED_BOOK) || conduit.is(Items.BOOK);
        return RuneItem.getEnchantment(rune)
            .filter(enchantment -> enchantment.value().canEnchant(conduit) || isBook)
            .filter(enchantment -> isValidEnchantForItem(conduit, enchantment))
            .isPresent();
    }

    public static boolean isValidEnchantForItem(ItemStack conduit, Holder<Enchantment> enchantment) {
        return conduit.getEnchantments().keySet().stream()
            .allMatch(conduitEnchantment ->
                conduitEnchantment.equals(enchantment) || Enchantment.areCompatible(conduitEnchantment, enchantment)
            );
    }

    public static int getResultEnchantmentLevel(ItemStack itemStack, Holder<Enchantment> entry, Enchantment enchantment, ItemStack rune) {
        if (rune.get(MagicRevamped.DataComponents.OPEN) == null) return 1;
        int currentLevel = EnchantmentHelper.getEnchantmentsForCrafting(itemStack).getLevel(entry);
        int maxLevel = rune.get(MagicRevamped.DataComponents.CHARGED) == null ? enchantment.getMaxLevel() : enchantment.getMaxLevel() + 1;
        return Math.min(currentLevel + 1, maxLevel);
    }

    @Override
    public void slotsChanged(@NonNull Container inventory) {
        if (this.filling) return;
        this.context.execute((world, _) -> {
            if (!(world instanceof ServerLevel serverWorld)) return;
            CraftingInput input = this.craftingInventory.asCraftInput();
            RecipeHolder<EnchantmentRecipe> recipe = serverWorld.recipeAccess().getRecipeFor(MagicRevamped.RecipeTypes.ENCHANTMENT_RECIPE_TYPE, input, serverWorld).orElse(null);
            ModEnchantmentScreenHandler.updateResult(this, serverWorld, this.player, this.craftingInventory, this.craftingResultInventory, recipe);
       });
    }

    public void onInputSlotFillStart() {
        this.filling = true;
    }

    public void onInputSlotFillFinish(ServerLevel world, RecipeHolder<EnchantmentRecipe> recipe) {
        this.filling = false;
        ModEnchantmentScreenHandler.updateResult(this, world, this.player, this.craftingInventory, this.craftingResultInventory, recipe);
    }

    public List<Slot> getInputSlots() {
        return this.slots.subList(1, 10);
    }
    public Slot getOutputSlot() {
        return this.slots.getFirst();
    }

    protected Player getPlayer() {
        return this.player;
    }

    @Override
    public @NonNull RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    public int getSeed() {
        return this.seed.get();
    }

    public int getFlux(EnchantmentCraftingContainer input, Level world, ItemStack stack, BlockPos blockPos) {
        int penalty = stack.is(MagicRevamped.Items.RUNE) ? 1 : 2;
        int flux = input.getNonEmptyAdditions()
            .map(itemStack ->
                 Optional.ofNullable(itemStack.get(MagicRevamped.DataComponents.FLUX)).map(RuneItem.Flux::value).orElse(0)
            )
            .reduce(0, Integer::sum);

        return flux * getLevelRequirement(input, world, blockPos) * penalty;
    }

    public static double getMoonBonus(MoonPhase moonPhase, boolean isNight) {
        if (!isNight) return 1;
        return switch (moonPhase) {
            case MoonPhase.FULL_MOON -> 0.51;
            case MoonPhase.WANING_GIBBOUS, MoonPhase.WAXING_GIBBOUS -> 0.91;
            case MoonPhase.THIRD_QUARTER, MoonPhase.FIRST_QUARTER -> 0.96;
            case MoonPhase.WANING_CRESCENT, MoonPhase.WAXING_CRESCENT -> 0.99;
            default -> 1;
        };
    }

    public static int getRuneCost(EnchantmentCraftingContainer input) {
        return input.getNonEmptyAdditions()
            .filter(itemStack -> itemStack.is(MagicRevamped.Items.RUNE))
            .map(RuneItem::getEnchantment)
            .flatMap(Optional::stream)
            .map(Holder::value)
            .map(Enchantment::getAnvilCost)
            .reduce(0, Integer::sum);
    }

    public static int getInputCost(EnchantmentCraftingContainer input) {
        return RuneItem.getEnchantments(input.getConduit())
            .map(RuneItem.LeveledEnchantment::cost)
            .reduce(0, (sum, i) -> 2 * sum + i);
    }

    public static int getLevelRequirement(EnchantmentCraftingContainer input, Level world, BlockPos blockPos) {
        double moonBonus = getMoonBonus(world.environmentAttributes().getDimensionValue(EnvironmentAttributes.MOON_PHASE), world.isDarkOutside());
        int cost = getRuneCost(input) + getInputCost(input);
        long helperBonus = world.getEntities(null, new AABB(blockPos).inflate(25))
            .stream()
            .filter(e -> e.is(MagicRevamped.EntityTags.ENCHANTMENT_HELPERS))
            .count();
        return Math.clamp(
            (int) Math.ceil(moonBonus * cost) - Math.clamp(helperBonus, 0, 4),
            0, 99
        );
    }

    public boolean doFluxCheck(Player player, EnchantmentCraftingContainer input, Level world, BlockPos blockPos, ItemStack stack) {
        int flux = this.getFlux(input, world, stack, blockPos);
        int playerSkill = player.getAttachedOrCreate(MagicRevamped.DataAttachments.ENCHANTMENT_SKILL);
        int playerCheck = player.getRandom().nextIntBetweenInclusive(0, 150 + playerSkill);
        int bookshelfBonus = this.getBookshelfBonus(world, blockPos);
        int bookshelfCheck = player.getRandom().nextIntBetweenInclusive(Math.floorDiv(bookshelfBonus, 10), bookshelfBonus);
        boolean success = (playerCheck + bookshelfCheck > flux);
        MagicRevamped.LOGGER.info("{} : [{}:{} + {}:{}] {} {}", success ? "Success!" : "Failure :(", playerCheck, 150 + playerSkill, bookshelfCheck, bookshelfBonus, success ? ">" : "<", flux);
        return success;
    }

    public void tickTimeout() {
        int timeout = this.timeout.get() - 1;
        this.timeout.set(Math.max(0, timeout));
    }

    public void onTakeResult(Player player, ItemStack stack) {
        this.timeout.set(MAX_TIME_OUT);
        this.context.execute((world, blockPos) -> takeResult(world, blockPos, player, stack));
        this.craftingInventory.setChanged();
    }

    public void takeResult(Level world, BlockPos blockPos, Player player, ItemStack stack) {
        String original = this.craftingInventory.getConduit().getEnchantments().toString();
        if(!this.doFluxCheck(player, this.craftingInventory, world, blockPos, stack)) {
            takeResultFail(original, world, blockPos, player, stack);
            return;
        }
        takeResultSuccess(original, stack);
        takeResultEnchanted(world, blockPos, stack);
    }

    public void triggerAdvancements(String original, ItemStack stack, boolean success, boolean decorationsPresent) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        ItemEnchantments newEnchantments = stack.getEnchantments();
        boolean changesMade = !newEnchantments.isEmpty() && !original.equals(newEnchantments.toString());
        MagicRevamped.CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, success, decorationsPresent, changesMade);
    }

    public void takeResultSuccess(String original, ItemStack stack) {
        triggerAdvancements(original, stack, true, false);
        boostPlayerEnchantingSkill(5);
    }

    public void takeResultFail(String original, Level world, BlockPos blockPos, Player player, ItemStack stack) {
        world.playSound(null, blockPos, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.BLOCKS, 0.5f, world.getRandom().nextFloat() * 0.1f + 0.9f);
        Consequence.Result<ItemStack> result = doConsequence(world, blockPos, player, stack);
        stack.setCount(result.entry().getCount());
        stack.applyComponents(result.entry().getComponents());
        player.giveExperienceLevels(-getLevelRequirement(this.craftingInventory, world, blockPos));
        triggerAdvancements(original, stack, false, result.decorationsPresent());
        boostPlayerEnchantingSkill(1);
        if (result.success()) {
            takeResultEnchanted(world, blockPos, stack);
        } else {
            ItemStack book = enchant(new ItemStack(Items.BOOK), this.craftingInventory);
            BlockState newState = world.getBlockState(blockPos).setValue(BlockStateProperties.HAS_BOOK, false);
            world.setBlock(blockPos, newState, 3);
            Util.drop(world, Vec3.atCenterOf(blockPos), book);
        }
    }

    public void takeResultEnchanted(Level world, BlockPos blockPos, ItemStack stack) {
        player.awardStat(Stats.ENCHANT_ITEM);
        world.playSound(null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.1f + 0.9f);
        this.resultSlot.checkTakeAchievements(stack);
        IntStream.range(0, this.craftingInventory.getContainerSize()).forEach(i -> this.craftingInventory.removeItem(i, 1));
    }

    public void boostPlayerEnchantingSkill(int value) {
        player.modifyAttached(MagicRevamped.DataAttachments.ENCHANTMENT_SKILL, skill -> Math.clamp(skill + value, 0, 100));
    }

    public Consequence.Result<ItemStack> doConsequence(Level world, BlockPos blockPos, Player player, ItemStack stack) {
        if (!(world instanceof ServerLevel serverWorld)) return new Consequence.Result<>(ItemStack.EMPTY, false, false);
        if (!(player instanceof ServerPlayer serverPlayer)) return new Consequence.Result<>(ItemStack.EMPTY, false, false);

        return Consequence.pick(world, blockPos).run(serverWorld, blockPos, serverPlayer, this.craftingInventory, stack);
    }

    public int getBookBonus(ItemStack itemStack) {
        List<Enchantment> enchantments = RuneItem.getEnchantments(itemStack)
            .map(RuneItem.LeveledEnchantment::enchantment)
            .map(Holder::value)
            .toList();
        int specialtyBonus = this.craftingInventory.getNonEmptyAdditions()
            .map(RuneItem::getEnchantment)
            .flatMap(Optional::stream)
            .map(Holder::value)
            .anyMatch(enchantments::contains) ? 10 : 0;

        return specialtyBonus + RuneItem.getEnchantments(itemStack)
            .map(RuneItem.LeveledEnchantment::level)
            .map(i -> BOOKSHELF_BONUSES.getOrDefault(i,1))
            .reduce(1, Integer::max);
    }

    public int getSingleBookshelfBonus(BlockInWorld block) {
        if(!(block.getEntity() instanceof ChiseledBookShelfBlockEntity chiseledBookshelf)) return 3;
        return chiseledBookshelf.getItems().stream()
            .filter(itemStack -> !itemStack.isEmpty())
            .map(this::getBookBonus)
            .reduce(0, Integer::sum);
    }

    public int getBookshelfBonus(Level level, BlockPos pos) {
        return MagicRevamped.POWER_PROVIDER_OFFSETS.stream()
             .map(blockPos -> new BlockInWorld(level, pos.offset(blockPos), false))
             .filter(block -> block.getState().is(BlockTags.ENCHANTMENT_POWER_PROVIDER))
             .map(this::getSingleBookshelfBonus)
             .reduce(0, Integer::sum);
    }

    public record PlaceEnchantmentRecipe(ModEnchantmentScreenHandler handler) implements ServerPlaceRecipe.CraftingMenuAccess<EnchantmentRecipe> {
        @Override
        public void fillCraftSlotsStackedContents(@NonNull StackedItemContents finder) {
            handler.fillCraftSlotsStackedContents(finder);
        }

        @Override
        public void clearCraftingContent() {
            handler.craftingInventory.clearContent();
        }

        @Override
        public boolean recipeMatches(@NonNull RecipeHolder<EnchantmentRecipe> entry) {
            return entry.value().matches(handler.craftingInventory.asCraftInput(), handler.getPlayer().level());
        }
    }
}

