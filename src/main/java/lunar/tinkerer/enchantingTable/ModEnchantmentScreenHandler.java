package lunar.tinkerer.enchantingTable;

import lunar.tinkerer.*;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.book.RecipeBookType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MoonPhase;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ModEnchantmentScreenHandler
        extends AbstractRecipeScreenHandler {
    public final ScreenHandlerContext context;
    public final PlayerEntity player;
    private boolean filling;
    public final static int MAX_TIME_OUT = 20;
    public final Property timeout = Property.create();
    public final Property seed = Property.create();
    public final RecipeInputInventory craftingInventory;
    protected final EnchantingTableResultInventory craftingResultInventory = new EnchantingTableResultInventory();
    public EnchantingResultSlot resultSlot;

    public ModEnchantmentScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public ModEnchantmentScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModBlockEntities.ENCHANTMENT_SCREEN_HANDLER, syncId);
        this.addProperty(this.seed).set(playerInventory.player.getEnchantingTableSeed());
        this.addProperty(this.timeout).set(0);
        this.context = context;
        this.player = playerInventory.player;
        this.craftingInventory = new RecipeInputInventory() {
            private final ScreenHandler handler = ModEnchantmentScreenHandler.this;
            private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(9, ItemStack.EMPTY);

            @Override
            public void provideRecipeInputs(RecipeFinder finder) {
                for (ItemStack itemStack : this.stacks) {
                    finder.addInputIfUsable(itemStack);
                }
            }

            @Override
            public void clear() {
                this.stacks.clear();
            }

            @Override
            public int size() {
                return 9;
            }

            @Override
            public boolean isEmpty() {
                return this.stacks.stream().allMatch(ItemStack::isEmpty);
            }

            @Override
            public ItemStack getStack(int slot) {
                if (slot >= this.size()) {
                    return ItemStack.EMPTY;
                }
                return this.stacks.get(slot);
            }

            @Override
            public ItemStack removeStack(int slot) {
                return Inventories.removeStack(this.stacks, slot);
            }


            @Override
            public ItemStack removeStack(int slot, int amount) {
                ItemStack itemStack = Inventories.splitStack(this.stacks, slot, amount);
                if (!itemStack.isEmpty()) {
                    this.handler.onContentChanged(this);
                }
                return itemStack;
            }

            @Override
            public void setStack(int slot, ItemStack stack) {
                this.stacks.set(slot, stack);
                this.handler.onContentChanged(this);
            }

            @Override
            public void markDirty() {
                ModEnchantmentScreenHandler.this.onContentChanged(this);
            }

            @Override
            public boolean canPlayerUse(PlayerEntity player) {
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
            public List<ItemStack> getHeldStacks() {
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
        this.addPlayerSlots(playerInventory, 8, 91);

        context.run((world, blockPos) -> {
            Objects.requireNonNull(world.getServer()).addServerGuiTickable(this::tickTimeout);
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
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.craftingInventory));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return ModEnchantmentScreenHandler.canUse(this.context, player, ModBlocks.ENCHANTING_TABLE);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        Slot sourceSlot = this.slots.get(slot);
        ItemStack original = sourceSlot.getStack().copy();
        ItemStack itemStack = switch (slot) {
            case 0 -> quickMoveFromResult(player, slot);
            case 1,2,3,4,5,6,7,8,9 -> quickMoveFromTable(slot);
            default -> quickMoveFromPlayer(slot);
        };

        sourceSlot.markDirty();

        if (original.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
        }

        if (slot == 0) {
            player.dropItem(itemStack, false);
        } else {
            //onTakeItem handled by quickMoveFromResult
            sourceSlot.onTakeItem(player, itemStack);
        }
        this.sendContentUpdates();
        return itemStack;
    }

    private ItemStack quickMoveFromResult(PlayerEntity player, int slot) {
        if(!this.resultSlot.canTakeItems(player)) return ItemStack.EMPTY;
        ItemStack items = this.resultSlot.getStack();
        if(items.isEmpty()) return ItemStack.EMPTY;
        this.resultSlot.onTakeItem(player, items);
        items.getItem().onCraftByPlayer(items, player);
        this.insertItem(items, 10, 46, true);
        this.onContentChanged(this.craftingInventory);
        return items;
    }

    private ItemStack quickMoveFromTable(int slot) {
        ItemStack items = this.slots.get(slot).getStack();
        this.insertItem(items, 10, 46, false);
        return items;
    }

    private ItemStack quickMoveFromPlayer(int slot) {
        ItemStack items = this.slots.get(slot).getStack();
        if(this.insertItem(items, 1, 10, false)) {
            return items;
        }

        if (slot < 37) {
            this.insertItem(items, 37, 46, false);
        } else {
            this.insertItem(items, 10, 37, false);
        }
        return items;
    }

    @Override
    public PostFillAction fillInputSlots(boolean craftAll, boolean creative, RecipeEntry<?> recipe, ServerWorld world, PlayerInventory inventory) {
        RecipeEntry<EnchantmentRecipe> recipeEntry = (RecipeEntry<EnchantmentRecipe>) recipe;
        this.onInputSlotFillStart();
        try {
            List<Slot> list = this.getInputSlots();
            return InputSlotFiller.fill(new InputSlotFiller.Handler<>(){
                @Override
                public void populateRecipeFinder(RecipeFinder finder) {
                    ModEnchantmentScreenHandler.this.populateRecipeFinder(finder);
                }

                @Override
                public void clear() {
                    ModEnchantmentScreenHandler.this.craftingInventory.clear();
                }

                @Override
                public boolean matches(RecipeEntry<EnchantmentRecipe> entry) {
                    return entry.value().matches(ModEnchantmentScreenHandler.this.craftingInventory.createRecipeInput(), ModEnchantmentScreenHandler.this.getPlayer().getEntityWorld());
                }
            }, 3, 3, list, list, inventory, recipeEntry, craftAll, creative);
        } finally {
            this.onInputSlotFillFinish(world, recipeEntry);
        }
    }

    @Override
    public void populateRecipeFinder(RecipeFinder finder) {
        this.craftingInventory.provideRecipeInputs(finder);
    }

    protected static void updateResult(ModEnchantmentScreenHandler handler, ServerWorld world, PlayerEntity player, RecipeInputInventory craftingInventory, EnchantingTableResultInventory resultInventory, @Nullable RecipeEntry<EnchantmentRecipe> recipe) {
        ItemStack conduit = craftingInventory.getStack(0);
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
        ItemStack itemStack;

        if (conduit.isEmpty()) {
            itemStack = ItemStack.EMPTY;
        } else if (conduit.isOf(Items.LAPIS_LAZULI)) {
            itemStack = carveRune(world, player, craftingInventory, resultInventory, recipe);
        } else if (conduit.isOf(ModItems.RUNE)) {
            itemStack = stabilize(craftingInventory);
        } else {
            itemStack = enchant(craftingInventory);
        }
        resultInventory.setStack(0, itemStack);
        handler.setReceivedStack(0, itemStack);
        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack));
    }

    private static ItemStack stabilize(RecipeInputInventory craftingInventory) {
        ItemStack conduit = craftingInventory.getStack(0);
        if (!craftingInventory.getHeldStacks().subList(1, 9).stream().allMatch(
            itemStack -> itemStack.isEmpty() || itemStack.isOf(Items.DIAMOND)
        )) {
            return ItemStack.EMPTY;
        }

        int stabilization = craftingInventory.getHeldStacks()
            .subList(1, 9)
            .stream()
            .filter(itemStack -> !itemStack.isEmpty())
            .filter(itemStack -> itemStack.isOf(Items.DIAMOND))
            .toList()
            .size();

        int currentFLux = Optional.ofNullable(conduit.get(ModItems.FLUX)).orElse(RuneItem.DEFAULT_RUNE_FLUX);
        var newFlux = Math.max(0, currentFLux - stabilization);

        if (newFlux == currentFLux) return ItemStack.EMPTY;

        ItemStack result = conduit.copyWithCount(1);
        result.set(ModItems.FLUX, newFlux);
        return result;
    }

    private static ItemStack carveRune(ServerWorld world, PlayerEntity player, RecipeInputInventory craftingInventory, EnchantingTableResultInventory resultInventory, @Nullable RecipeEntry<EnchantmentRecipe> recipe) {
        CraftingRecipeInput craftingRecipeInput = craftingInventory.createRecipeInput();
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
        ItemStack itemStack = ItemStack.EMPTY;
        if(recipe == null) {
            return itemStack;
        }
        Optional<RecipeEntry<EnchantmentRecipe>> optional = world.getServer().getRecipeManager().getFirstMatch(
                ModRecipeTypes.ENCHANTMENT_RECIPE_TYPE,
                craftingRecipeInput,
                world,
                recipe.id()
        );
        if (optional.isPresent()) {
            ItemStack itemStack2;
            RecipeEntry<EnchantmentRecipe> recipeEntry = optional.get();
            EnchantmentRecipe craftingRecipe = recipeEntry.value();
            if (resultInventory.shouldCraftRecipe(serverPlayerEntity, recipeEntry) && (itemStack2 = craftingRecipe.craft(craftingRecipeInput, world.getRegistryManager())).isItemEnabled(world.getEnabledFeatures())) {
                itemStack = itemStack2;
            }
        }
        return itemStack;
    }

    public static ItemStack enchant(RecipeInputInventory craftingInventory) {
        ItemStack conduit = craftingInventory.getStack(0);
        if (!EnchantmentHelper.canHaveEnchantments(conduit) && !conduit.isOf(Items.BOOK)) {
            return ItemStack.EMPTY;
        }

        if (!craftingInventory.getHeldStacks().subList(1, 9).stream().allMatch(
                itemStack -> itemStack.isEmpty() || isValidRuneForItem(itemStack, conduit)
        )) {
            return ItemStack.EMPTY;
        }

        List<ItemStack> inputs = craftingInventory.getHeldStacks()
                .subList(1, 9)
                .stream()
                .filter(itemStack -> !itemStack.isEmpty())
                .filter(itemStack -> itemStack.isOf(ModItems.RUNE))
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
                conduit.isOf(Items.BOOK) ? new ItemStack(Items.ENCHANTED_BOOK) : conduit.copy(),
                (subResult, rune) -> {
                    RegistryEntry<Enchantment> entry = rune.get(ModItems.ENCHANTMENT);
                    Enchantment enchantment = entry != null ? entry.value() : null;
                    if (!isValidRuneForItem(rune, subResult)) return subResult;
                    int nextLevel = getResultEnchantmentLevel(subResult, entry, enchantment, rune);
                    EnchantmentHelper.apply(subResult, builder -> builder.add(entry, nextLevel));
                    return subResult;
                }
            );

        if (result.isDamageable()) {
            result.set(DataComponentTypes.MAX_DAMAGE, Math.max(result.getMaxDamage() - 1, 1));
        }

        return result;
    }

    public static boolean isValidRuneForItem(ItemStack rune, ItemStack conduit) {
        if (!rune.isOf(ModItems.RUNE)) return false;
        Optional<RegistryEntry<Enchantment>> runeEnchantmentRegistryEntryOptional = Optional
                .ofNullable(rune.get(ModItems.ENCHANTMENT));
        if (runeEnchantmentRegistryEntryOptional.isEmpty()) return false;
        boolean isAcceptable = runeEnchantmentRegistryEntryOptional
            .map(RegistryEntry::value)
            .map(enchantment ->
                 enchantment.isAcceptableItem(conduit) || conduit.isOf(Items.ENCHANTED_BOOK) || conduit.isOf(Items.BOOK)
            )
            .orElse(false);
        if (!isAcceptable) return false;
        RegistryEntry<Enchantment> runeEnchantmentRegistryEntry = runeEnchantmentRegistryEntryOptional.get();
        return conduit.getEnchantments().getEnchantments().stream()
                .allMatch(conduitEnchantment ->
                        conduitEnchantment.equals(runeEnchantmentRegistryEntry) ||
                    Enchantment.canBeCombined(conduitEnchantment, runeEnchantmentRegistryEntry)
                );
    }

    public static int getResultEnchantmentLevel(ItemStack itemStack, RegistryEntry<Enchantment> entry, Enchantment enchantment, ItemStack rune) {
        if (rune.get(ModItems.OPEN) == null) return 1;
        int currentLevel = EnchantmentHelper.getEnchantments(itemStack).getLevel(entry);
        int maxLevel = rune.get(ModItems.CHARGED) == null ? enchantment.getMaxLevel() : enchantment.getMaxLevel() + 1;
        return Math.min(currentLevel + 1, maxLevel);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        if (this.filling) { return; }
        this.context.run((world, pos) -> {
            if (world instanceof ServerWorld serverWorld) {
                CraftingRecipeInput input = this.craftingInventory.createRecipeInput();
                RecipeEntry<EnchantmentRecipe> recipe = serverWorld.getRecipeManager().getFirstMatch(ModRecipeTypes.ENCHANTMENT_RECIPE_TYPE, input, serverWorld).orElse(null);
                ModEnchantmentScreenHandler.updateResult(this, serverWorld, this.player, this.craftingInventory, this.craftingResultInventory, recipe);
            }
        });
    }

    public void onInputSlotFillStart() {
        this.filling = true;
    }

    public void onInputSlotFillFinish(ServerWorld world, RecipeEntry<EnchantmentRecipe> recipe) {
        this.filling = false;
        ModEnchantmentScreenHandler.updateResult(this, world, this.player, this.craftingInventory, this.craftingResultInventory, recipe);
    }

    public List<Slot> getInputSlots() {
        return this.slots.subList(1, 10);
    }
    public Slot getOutputSlot() {
        return this.slots.getFirst();
    }

    protected PlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public RecipeBookType getCategory() {
        return RecipeBookType.CRAFTING;
    }

    public int getSeed() {
        return this.seed.get();
    }

    public static int getFlux(RecipeInputInventory input, World world) {
        int flux = input.getHeldStacks()
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

    public static int getRuneCost(RecipeInputInventory input) {
        return input.getHeldStacks().stream()
                .filter(itemStack -> itemStack.isOf(ModItems.RUNE))
                .map(itemStack -> itemStack.get(ModItems.ENCHANTMENT))
                .filter(Objects::nonNull)
                .map(RegistryEntry::value)
                .filter(Objects::nonNull)
                .map(Enchantment::getAnvilCost)
                .reduce(0, (integer, integer2) ->
                        2 * integer + integer2
                );
    }

    public static int getInputCost(RecipeInputInventory input) {
        return RuneItem.getEnchantments(input.getStack(0))
                .map(leveledEnchantment -> {
                    int level = leveledEnchantment.level();
                    int enchantmentCost = Optional
                            .of(leveledEnchantment)
                            .map(RuneItem.LeveledEnchantment::enchantment)
                            .map(RegistryEntry::value)
                            .map(Enchantment::getAnvilCost)
                            .orElse(8);
                    return level * enchantmentCost;
                })
                .reduce(0, Integer::sum);
    }

    public static int getLevelRequirement(RecipeInputInventory input, World world) {
        return Math.clamp(
            (int) Math.ceil(getMoonBonus(world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.MOON_PHASE_VISUAL), world.isNight()) * (getRuneCost(input) + getInputCost(input))),
            0, 99
        );
    }

    public boolean doFluxCheck(
        PlayerEntity player,
        RecipeInputInventory input,
        World world,
        BlockPos blockPos
    ) {
        int flux = ModEnchantmentScreenHandler.getFlux(input, world);
        int playerCheck = player.getRandom().nextBetween(0, 1000);
        int bookshelfBonus = this.getBookshelfBonus(world, blockPos);
        int bookshelfCheck = player.getRandom().nextBetween(Math.floorDiv(bookshelfBonus, 10), bookshelfBonus);
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

    public void onTakeResult(PlayerEntity player, ItemStack stack) {
        this.context.run(((world, blockPos) -> {
            this.timeout.set(MAX_TIME_OUT);
            boolean success = this.doFluxCheck(player, this.craftingInventory, world, blockPos);
            if (!success) {
                world.playSound(null, blockPos, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.1f + 0.9f);
                Consequence.Result<ItemStack> result = doConsequence(world, blockPos, player, stack);
                stack.setCount(result.entry().getCount());
                stack.applyComponentsFrom(result.entry().getComponents());
                player.addExperienceLevels(-getLevelRequirement(this.craftingInventory, world));
                if (!result.success()) return;
            }

            player.incrementStat(Stats.ENCHANT_ITEM);
            world.playSound(null, blockPos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.1f + 0.9f);
            this.resultSlot.onCrafted(stack);
            IntStream.range(0, this.craftingInventory.size()).forEach(
                i -> this.craftingInventory.removeStack(i, 1)
            );
        }));
        this.craftingInventory.markDirty();
    }

    public Consequence.Result<ItemStack> doConsequence(World world, BlockPos blockPos, PlayerEntity player, ItemStack stack) {
        if (!(world instanceof ServerWorld serverWorld)) return new Consequence.Result<>(ItemStack.EMPTY, false);
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return new Consequence.Result<>(ItemStack.EMPTY, false);

        Consequence consequence = ConsequenceManager.pick(
            world,
            ModEnchantingTableBlock.DECORATION_OFFSETS.stream()
                .map(blockPos1 -> blockPos1.add(blockPos))
                .toList()
        );
        return consequence.run(serverWorld, blockPos, serverPlayer, this.craftingInventory, stack);
    }

    public int getBookBonus(ItemStack itemStack) {
        List<RuneItem.LeveledEnchantment> enchantments = RuneItem.getEnchantments(itemStack).toList();
        int specialtyBonus = this.craftingInventory.getHeldStacks().subList(1,9).stream()
            .map(itemStack1 -> Optional.ofNullable(itemStack1.get(ModItems.ENCHANTMENT)).map(RegistryEntry::value))
            .anyMatch(
                enchantment -> enchantments.stream()
                    .map(RuneItem.LeveledEnchantment::enchantment)
                    .map(RegistryEntry::value)
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

    public int getSingleBookshelfBonus(World world, BlockPos blockPos) {
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if(!(blockEntity instanceof ChiseledBookshelfBlockEntity chiseledBookshelfBlockEntity)) {
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

