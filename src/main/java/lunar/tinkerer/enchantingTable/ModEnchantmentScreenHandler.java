package lunar.tinkerer.enchantingTable;

import lunar.tinkerer.*;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ModEnchantmentScreenHandler
        extends RecipeBookMenu {
    public final ContainerLevelAccess context;
    public final Player player;
    private boolean filling;
    public final static int MAX_TIME_OUT = 20;
    public final DataSlot timeout = DataSlot.standalone();
    public final DataSlot seed = DataSlot.standalone();
    public final CraftingContainer craftingInventory;
    protected final EnchantingTableResultInventory craftingResultInventory = new EnchantingTableResultInventory();
    public EnchantingResultSlot resultSlot;

    public ModEnchantmentScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ModEnchantmentScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(ModBlockEntities.ENCHANTMENT_SCREEN_HANDLER, syncId);
        this.addDataSlot(this.seed).set(playerInventory.player.getEnchantmentSeed());
        this.addDataSlot(this.timeout).set(0);
        this.context = context;
        this.player = playerInventory.player;
        this.craftingInventory = new CraftingContainer() {
            private final AbstractContainerMenu handler = ModEnchantmentScreenHandler.this;
            private final NonNullList<ItemStack> stacks = NonNullList.withSize(9, ItemStack.EMPTY);

            @Override
            public void fillStackedContents(StackedItemContents finder) {
                for (ItemStack itemStack : this.stacks) {
                    finder.accountSimpleStack(itemStack);
                }
            }

            @Override
            public void clearContent() {
                this.stacks.clear();
            }

            @Override
            public int getContainerSize() {
                return 9;
            }

            @Override
            public boolean isEmpty() {
                return this.stacks.stream().allMatch(ItemStack::isEmpty);
            }

            @Override
            public ItemStack getItem(int slot) {
                if (slot >= this.getContainerSize()) {
                    return ItemStack.EMPTY;
                }
                return this.stacks.get(slot);
            }

            @Override
            public ItemStack removeItemNoUpdate(int slot) {
                return ContainerHelper.takeItem(this.stacks, slot);
            }


            @Override
            public ItemStack removeItem(int slot, int amount) {
                ItemStack itemStack = ContainerHelper.removeItem(this.stacks, slot, amount);
                if (!itemStack.isEmpty()) {
                    this.handler.slotsChanged(this);
                }
                return itemStack;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                this.stacks.set(slot, stack);
                this.handler.slotsChanged(this);
            }

            @Override
            public void setChanged() {
                ModEnchantmentScreenHandler.this.slotsChanged(this);
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }

            @Override
            public int getWidth() {
                return 3;
            }

            @Override
            public int getHeight() {
                return 3;
            }

            @Override
            public List<ItemStack> getItems() {
                return List.copyOf(this.stacks);
            }
        };

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
        this.addInputSlots(44,24);
        this.addStandardInventorySlots(playerInventory, 8, 91);

        context.execute((world, blockPos) -> {
            Objects.requireNonNull(world.getServer()).addTickable(this::tickTimeout);
        });
    }

    protected void addResultSlot() {
        this.addSlot(this.resultSlot);
    }

    protected void addInputSlots(int x, int y) {
        int offset = 30;
        int corner = 24;
        this.addSlot(new Slot(this.craftingInventory, 0, x, y));                    //center
        this.addSlot(new Slot(this.craftingInventory, 1, x, y-offset));          //top
        this.addSlot(new Slot(this.craftingInventory, 2,x+corner, y-corner)); //top right
        this.addSlot(new Slot(this.craftingInventory, 3,x+offset, y));           //right
        this.addSlot(new Slot(this.craftingInventory, 4,x+corner, y+corner)); //bottom right
        this.addSlot(new Slot(this.craftingInventory, 5, x, y+offset));          //bottom
        this.addSlot(new Slot(this.craftingInventory, 6,x-corner, y+corner)); //bottom left
        this.addSlot(new Slot(this.craftingInventory, 7,x-offset, y));           //left
        this.addSlot(new Slot(this.craftingInventory, 8,x-corner, y-corner)); //top left
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.context.execute((world, pos) -> this.clearContainer(player, this.craftingInventory));
    }

    @Override
    public boolean stillValid(Player player) {
        return ModEnchantmentScreenHandler.stillValid(this.context, player, ModBlocks.ENCHANTING_TABLE);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        Slot sourceSlot = this.slots.get(slot);
        ItemStack original = sourceSlot.getItem().copy();
        ItemStack itemStack = switch (slot) {
            case 0 -> quickMoveFromResult(player, slot);
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

    private ItemStack quickMoveFromResult(Player player, int slot) {
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
    public PostPlaceAction handlePlacement(boolean craftAll, boolean creative, RecipeHolder<?> recipe, ServerLevel world, Inventory inventory) {
        RecipeHolder<EnchantmentRecipe> recipeEntry = (RecipeHolder<EnchantmentRecipe>) recipe;
        this.onInputSlotFillStart();
        try {
            List<Slot> list = this.getInputSlots();
            return ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<>(){
                @Override
                public void fillCraftSlotsStackedContents(StackedItemContents finder) {
                    ModEnchantmentScreenHandler.this.fillCraftSlotsStackedContents(finder);
                }

                @Override
                public void clearCraftingContent() {
                    ModEnchantmentScreenHandler.this.craftingInventory.clearContent();
                }

                @Override
                public boolean recipeMatches(RecipeHolder<EnchantmentRecipe> entry) {
                    return entry.value().matches(ModEnchantmentScreenHandler.this.craftingInventory.asCraftInput(), ModEnchantmentScreenHandler.this.getPlayer().level());
                }
            }, 3, 3, list, list, inventory, recipeEntry, craftAll, creative);
        } finally {
            this.onInputSlotFillFinish(world, recipeEntry);
        }
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents finder) {
        this.craftingInventory.fillStackedContents(finder);
    }

    protected static void updateResult(ModEnchantmentScreenHandler handler, ServerLevel world, Player player, CraftingContainer craftingInventory, EnchantingTableResultInventory resultInventory, @Nullable RecipeHolder<EnchantmentRecipe> recipe) {
        ItemStack conduit = craftingInventory.getItem(0);
        ServerPlayer serverPlayerEntity = (ServerPlayer)player;
        ItemStack itemStack;

        if (conduit.isEmpty()) {
            itemStack = ItemStack.EMPTY;
        } else if (conduit.is(Items.LAPIS_LAZULI)) {
            itemStack = carveRune(world, player, craftingInventory, resultInventory, recipe);
        } else if (conduit.is(ModItems.RUNE)) {
            itemStack = stabilize(craftingInventory);
        } else {
            itemStack = enchant(craftingInventory);
        }
        resultInventory.setItem(0, itemStack);
        handler.setRemoteSlot(0, itemStack);
        serverPlayerEntity.connection.send(new ClientboundContainerSetSlotPacket(handler.containerId, handler.incrementStateId(), 0, itemStack));
    }

    private static ItemStack stabilize(CraftingContainer craftingInventory) {
        ItemStack conduit = craftingInventory.getItem(0);
        if (!craftingInventory.getItems().subList(1, 9).stream().allMatch(
            itemStack -> itemStack.isEmpty() || itemStack.is(Items.DIAMOND)
        )) {
            return ItemStack.EMPTY;
        }

        int stabilization = craftingInventory.getItems()
            .subList(1, 9)
            .stream()
            .filter(itemStack -> !itemStack.isEmpty())
            .filter(itemStack -> itemStack.is(Items.DIAMOND))
            .toList()
            .size();

        int currentFLux = Optional.ofNullable(conduit.get(ModItems.FLUX)).orElse(RuneItem.DEFAULT_RUNE_FLUX);
        var newFlux = Math.max(0, currentFLux - stabilization);

        if (newFlux == currentFLux) return ItemStack.EMPTY;

        ItemStack result = conduit.copyWithCount(1);
        result.set(ModItems.FLUX, newFlux);
        return result;
    }

    private static ItemStack carveRune(ServerLevel world, Player player, CraftingContainer craftingInventory, EnchantingTableResultInventory resultInventory, @Nullable RecipeHolder<EnchantmentRecipe> recipe) {
        CraftingInput craftingRecipeInput = craftingInventory.asCraftInput();
        ServerPlayer serverPlayerEntity = (ServerPlayer)player;
        ItemStack itemStack = ItemStack.EMPTY;
        if(recipe == null) {
            return itemStack;
        }
        Optional<RecipeHolder<EnchantmentRecipe>> optional = world.getServer().getRecipeManager().getRecipeFor(
                ModRecipeTypes.ENCHANTMENT_RECIPE_TYPE,
                craftingRecipeInput,
                world,
                recipe.id()
        );
        if (optional.isPresent()) {
            ItemStack itemStack2;
            RecipeHolder<EnchantmentRecipe> recipeEntry = optional.get();
            EnchantmentRecipe craftingRecipe = recipeEntry.value();
            if (resultInventory.setRecipeUsed(serverPlayerEntity, recipeEntry) && (itemStack2 = craftingRecipe.assemble(craftingRecipeInput)).isItemEnabled(world.enabledFeatures())) {
                itemStack = itemStack2;
            }
        }
        return itemStack;
    }

    public static ItemStack enchant(CraftingContainer craftingInventory) {
        ItemStack conduit = craftingInventory.getItem(0);
        if (!EnchantmentHelper.canStoreEnchantments(conduit) && !conduit.is(Items.BOOK)) {
            return ItemStack.EMPTY;
        }

        if (!craftingInventory.getItems().subList(1, 9).stream().allMatch(
                itemStack -> itemStack.isEmpty() || isValidRuneForItem(itemStack, conduit)
        )) {
            return ItemStack.EMPTY;
        }

        List<ItemStack> inputs = craftingInventory.getItems()
                .subList(1, 9)
                .stream()
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.is(ModItems.RUNE))
                .toList();
        if (inputs.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = inputs.stream()
            .filter(itemStack -> itemStack.get(ModItems.ENCHANTMENT) != null)
            .sorted((itemStack1, itemStack2)  -> {
                var open1 = itemStack1.get(ModItems.OPEN) != null ? 1 : 0;
                var open2 = itemStack2.get(ModItems.OPEN) != null ? 1 : 0;
                var charged1 = itemStack1.get(ModItems.CHARGED) != null ? 10 : 0;
                var charged2 = itemStack2.get(ModItems.CHARGED) != null ? 10 : 0;
                return open1 + charged1 - open2 - charged2;
            })
            .reduce(
                conduit.is(Items.BOOK) ? new ItemStack(Items.ENCHANTED_BOOK) : conduit.copy(),
                (subResult, rune) -> {
                    Holder<Enchantment> entry = rune.get(ModItems.ENCHANTMENT);
                    Enchantment enchantment = entry != null ? entry.value() : null;
                    if (!isValidRuneForItem(rune, subResult)) return subResult;
                    int nextLevel = getResultEnchantmentLevel(subResult, entry, enchantment, rune);
                    EnchantmentHelper.updateEnchantments(subResult, builder -> builder.upgrade(entry, nextLevel));
                    return subResult;
                }
            );

        if (result.isDamageableItem()) {
            result.set(DataComponents.MAX_DAMAGE, Math.max(result.getMaxDamage() - 1, 1));
        }

        return result;
    }

    public static boolean isValidRuneForItem(ItemStack rune, ItemStack conduit) {
        if (!rune.is(ModItems.RUNE)) return false;
        Optional<Holder<Enchantment>> runeEnchantmentRegistryEntryOptional = Optional
                .ofNullable(rune.get(ModItems.ENCHANTMENT));
        if (runeEnchantmentRegistryEntryOptional.isEmpty()) return false;
        boolean isAcceptable = runeEnchantmentRegistryEntryOptional
            .map(Holder::value)
            .map(enchantment ->
                 enchantment.canEnchant(conduit) || conduit.is(Items.ENCHANTED_BOOK) || conduit.is(Items.BOOK)
            )
            .orElse(false);
        if (!isAcceptable) return false;
        Holder<Enchantment> runeEnchantmentRegistryEntry = runeEnchantmentRegistryEntryOptional.get();
        return conduit.getEnchantments().keySet().stream()
                .allMatch(conduitEnchantment ->
                        conduitEnchantment.equals(runeEnchantmentRegistryEntry) ||
                    Enchantment.areCompatible(conduitEnchantment, runeEnchantmentRegistryEntry)
                );
    }

    public static int getResultEnchantmentLevel(ItemStack itemStack, Holder<Enchantment> entry, Enchantment enchantment, ItemStack rune) {
        if (rune.get(ModItems.OPEN) == null) return 1;
        int currentLevel = EnchantmentHelper.getEnchantmentsForCrafting(itemStack).getLevel(entry);
        int maxLevel = rune.get(ModItems.CHARGED) == null ? enchantment.getMaxLevel() : enchantment.getMaxLevel() + 1;
        return Math.min(currentLevel + 1, maxLevel);
    }

    @Override
    public void slotsChanged(Container inventory) {
        if (this.filling) { return; }
        this.context.execute((world, pos) -> {
            if (world instanceof ServerLevel serverWorld) {
                CraftingInput input = this.craftingInventory.asCraftInput();
                RecipeHolder<EnchantmentRecipe> recipe = serverWorld.recipeAccess().getRecipeFor(ModRecipeTypes.ENCHANTMENT_RECIPE_TYPE, input, serverWorld).orElse(null);
                ModEnchantmentScreenHandler.updateResult(this, serverWorld, this.player, this.craftingInventory, this.craftingResultInventory, recipe);
            }
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
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    public int getSeed() {
        return this.seed.get();
    }

    public static int getFlux(CraftingContainer input, Level world) {
        int flux = input.getItems()
            .subList(1, 9)
            .stream()
            .map(itemStack ->
                 Optional.ofNullable(itemStack.get(ModItems.FLUX)).orElse(0)
            )
            .reduce(0, Integer::sum);

        return flux * getLevelRequirement(input, world);
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

    public static int getRuneCost(CraftingContainer input) {
        return input.getItems().stream()
                .filter(itemStack -> itemStack.is(ModItems.RUNE))
                .map(itemStack -> itemStack.get(ModItems.ENCHANTMENT))
                .filter(Objects::nonNull)
                .map(Holder::value)
                .filter(Objects::nonNull)
                .map(Enchantment::getAnvilCost)
                .reduce(0, (integer, integer2) ->
                        2 * integer + integer2
                );
    }

    public static int getInputCost(CraftingContainer input) {
        return RuneItem.getEnchantments(input.getItem(0))
                .map(leveledEnchantment -> {
                    int level = leveledEnchantment.level();
                    int enchantmentCost = Optional
                            .of(leveledEnchantment)
                            .map(RuneItem.LeveledEnchantment::enchantment)
                            .map(Holder::value)
                            .map(Enchantment::getAnvilCost)
                            .orElse(8);
                    return level * enchantmentCost;
                })
                .reduce(0, Integer::sum);
    }

    public static int getLevelRequirement(CraftingContainer input, Level world) {
        return Math.clamp(
            (int) Math.ceil(getMoonBonus(world.environmentAttributes().getDimensionValue(EnvironmentAttributes.MOON_PHASE), world.isDarkOutside()) * (getRuneCost(input) + getInputCost(input))),
            0, 99
        );
    }

    public boolean doFluxCheck(
        Player player,
        CraftingContainer input,
        Level world,
        BlockPos blockPos
    ) {
        int flux = ModEnchantmentScreenHandler.getFlux(input, world);
        int playerCheck = player.getRandom().nextIntBetweenInclusive(0, 1000);
        int bookshelfBonus = this.getBookshelfBonus(world, blockPos);
        int bookshelfCheck = player.getRandom().nextIntBetweenInclusive(Math.floorDiv(bookshelfBonus, 10), bookshelfBonus);
        boolean success = (playerCheck + bookshelfCheck > flux);
        MagicRevamped.LOGGER.info(
            "{} : [{}:1000 + {}:{}] {} {}",
            success ? "Success!" : "Failure :(",
            playerCheck,
            bookshelfCheck,
            bookshelfBonus,
            success ? ">" : "<",
            flux
        );
        return success;
    }

    public void tickTimeout() {
        int timeout = this.timeout.get() - 1;
        this.timeout.set(Math.max(0, timeout));
    }

    public void onTakeResult(Player player, ItemStack stack) {
        this.context.execute(((world, blockPos) -> {
            this.timeout.set(MAX_TIME_OUT);
            boolean success = this.doFluxCheck(player, this.craftingInventory, world, blockPos);
            if (!success) {
                world.playSound(null, blockPos, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.1f + 0.9f);
                Consequence.Result<ItemStack> result = doConsequence(world, blockPos, player, stack);
                stack.setCount(result.entry().getCount());
                stack.applyComponents(result.entry().getComponents());
                player.giveExperienceLevels(-getLevelRequirement(this.craftingInventory, world));
                if (!result.success()) return;
            }

            player.awardStat(Stats.ENCHANT_ITEM);
            world.playSound(null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.1f + 0.9f);
            this.resultSlot.checkTakeAchievements(stack);
            IntStream.range(0, this.craftingInventory.getContainerSize()).forEach(
                i -> this.craftingInventory.removeItem(i, 1)
            );
        }));
        this.craftingInventory.setChanged();
    }

    public Consequence.Result<ItemStack> doConsequence(Level world, BlockPos blockPos, Player player, ItemStack stack) {
        if (!(world instanceof ServerLevel serverWorld)) return new Consequence.Result<>(ItemStack.EMPTY, false);
        if (!(player instanceof ServerPlayer serverPlayer)) return new Consequence.Result<>(ItemStack.EMPTY, false);

        Consequence consequence = ConsequenceManager.pick(
            world,
            ModEnchantingTableBlock.DECORATION_OFFSETS.stream()
                .map(blockPos1 -> blockPos1.offset(blockPos))
                .toList()
        );
        return consequence.run(serverWorld, blockPos, serverPlayer, this.craftingInventory, stack);
    }

    public int getBookBonus(ItemStack itemStack) {
        List<RuneItem.LeveledEnchantment> enchantments = RuneItem.getEnchantments(itemStack).toList();
        int specialtyBonus = this.craftingInventory.getItems().subList(1,9).stream()
            .map(itemStack1 -> Optional.ofNullable(itemStack1.get(ModItems.ENCHANTMENT)).map(Holder::value))
            .anyMatch(
                enchantment -> enchantments.stream()
                    .map(RuneItem.LeveledEnchantment::enchantment)
                    .map(Holder::value)
                    .anyMatch(enchantment1 -> enchantment.filter(value -> value == enchantment1).isPresent())
            ) ? 10 : 0;

        return specialtyBonus + enchantments.stream()
                .map(RuneItem.LeveledEnchantment::level)
                .map(i -> switch (i) {
                    case 1 -> 3;
                    case 2 -> 5;
                    case 3 -> 8;
                    case 4 -> 13;
                    case 5 -> 21;
                    case 6 -> 34;
                    default -> 1;
                })
                .reduce(1, Integer::max);
    }

    public int getSingleBookshelfBonus(Level world, BlockPos blockPos) {
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if(!(blockEntity instanceof ChiseledBookShelfBlockEntity chiseledBookshelfBlockEntity)) {
            return 3;
        }
        AtomicInteger levelSum = new AtomicInteger();
        chiseledBookshelfBlockEntity.forEach(itemStack -> {
            if(itemStack.isEmpty()) return;
            int maxEnchantLevel = getBookBonus(itemStack);
            levelSum.addAndGet(maxEnchantLevel);
        });
        return levelSum.get();
    }

    public int getBookshelfBonus(Level world, BlockPos blockPos) {
        return ModEnchantingTableBlock.POWER_PROVIDER_OFFSETS.stream()
                                                             .map(blockPos1 -> blockPos1.offset(blockPos))
                                                             .filter(blockPos1 ->
                                                                         world.getBlockState(blockPos1)
                                                                              .is(BlockTags.ENCHANTMENT_POWER_PROVIDER)
                                                             )
                                                             .map(blockPos1 -> this.getSingleBookshelfBonus(world, blockPos1))
                                                             .reduce(0, Integer::sum);
    }
}

