package lunar.tinkerer;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ModEnchantmentScreenHandler
        extends AbstractRecipeScreenHandler {
    private final ScreenHandlerContext context;
    private final PlayerEntity player;
    private boolean filling;
    protected final RecipeInputInventory craftingInventory;
    protected final EnchantingTableResultInventory craftingResultInventory = new EnchantingTableResultInventory();

    public ModEnchantmentScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public ModEnchantmentScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModBlockEntities.ENCHANTMENT_SCREEN_HANDLER, syncId);
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

        this.addResultSlot(this.getPlayer(), 127, 32);
        this.addInputSlots(44,32);
        this.addPlayerSlots(playerInventory, 8, 99);
    }

    protected Slot addResultSlot(PlayerEntity player, int x, int y) {
        return this.addSlot(new CraftingResultSlot(player, this.craftingInventory, this.craftingResultInventory, 0, x, y) {
            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                this.onCrafted(stack);
                CraftingRecipeInput.Positioned positioned = ModEnchantmentScreenHandler.this.craftingInventory.createPositionedRecipeInput();
                CraftingRecipeInput craftingRecipeInput = positioned.input();
                int i = positioned.left();
                int j = positioned.top();
                DefaultedList<ItemStack> defaultedList = this.getRecipeRemainders(craftingRecipeInput, player.getWorld());

                for (int k = 0; k < craftingRecipeInput.getHeight(); ++k) {
                    for (int l = 0; l < craftingRecipeInput.getWidth(); ++l) {
                        int m = l + i + (k + j) * ModEnchantmentScreenHandler.this.craftingInventory.getWidth();
                        ItemStack itemStack = ModEnchantmentScreenHandler.this.craftingInventory.getStack(m);
                        ItemStack itemStack2 = defaultedList.get(l + k * craftingRecipeInput.getWidth());
                        if (!itemStack.isEmpty()) {
                            ModEnchantmentScreenHandler.this.craftingInventory.removeStack(m, 1);
                            itemStack = ModEnchantmentScreenHandler.this.craftingInventory.getStack(m);
                        }
                        if (itemStack2.isEmpty()) continue;
                        if (itemStack.isEmpty()) {
                            ModEnchantmentScreenHandler.this.craftingInventory.setStack(m, itemStack2);
                            continue;
                        }
                        if (ItemStack.areItemsAndComponentsEqual(itemStack, itemStack2)) {
                            itemStack2.increment(itemStack.getCount());
                            ModEnchantmentScreenHandler.this.craftingInventory.setStack(m, itemStack2);
                            continue;
                        }
                        if (player.getInventory().insertStack(itemStack2)) continue;
                        player.dropItem(itemStack2, false);
                    }
                }
            }

            private DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input, World world) {
                if (world instanceof ServerWorld serverWorld) {
                    return serverWorld.getRecipeManager().getFirstMatch(ModRecipeTypes.ENCHANTMENT_RECIPE_TYPE, input, serverWorld).map(recipe -> (recipe.value()).getRecipeRemainders(input)).orElse(DefaultedList.ofSize(input.size(), ItemStack.EMPTY));
                }
                return CraftingRecipe.collectRecipeRemainders(input);
            }
        });
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
        if (itemStack.isEmpty()) {
            sourceSlot.setStack(ItemStack.EMPTY);
        } else {
            sourceSlot.markDirty();
        }
        if (original.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
        }
        sourceSlot.onTakeItem(player, itemStack);
        if (slot == 0) {
            player.dropItem(itemStack, false);
        }
        this.sendContentUpdates();
        return itemStack;
    }

    private ItemStack quickMoveFromResult(PlayerEntity player, int slot) {
        ItemStack items = this.slots.get(slot).getStack();
        items.getItem().onCraftByPlayer(items, player);
        this.insertItem(items, 10, 46, true);
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
                    return entry.value().matches(ModEnchantmentScreenHandler.this.craftingInventory.createRecipeInput(), ModEnchantmentScreenHandler.this.getPlayer().getWorld());
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
        //TODO: Validate player has enough levels
        if (conduit.isEmpty()) {
            itemStack = ItemStack.EMPTY;
        } else if (conduit.isOf(Items.LAPIS_LAZULI)) {
            MagicRevamped.LOGGER.info("Rune Carving!");
            itemStack = carveRune(world, player, craftingInventory, resultInventory, recipe);
        } else {
            MagicRevamped.LOGGER.info("Enchanting!");
            itemStack = enchant(craftingInventory);
        }
        resultInventory.setStack(0, itemStack);
        handler.setReceivedStack(0, itemStack);
        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack));
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
        if (!EnchantmentHelper.canHaveEnchantments(conduit)) {
            return ItemStack.EMPTY;
        }

        if (!craftingInventory.getHeldStacks().subList(1, 9).stream().allMatch(
                itemStack -> itemStack.isEmpty() || itemStack.isOf(ModItems.RUNE)
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
                .reduce( conduit.copy(),
                        (subResult, rune) -> {
                    RegistryEntry<Enchantment> entry = rune.get(ModItems.ENCHANTMENT);
                    Enchantment enchantment = entry != null ? entry.value() : null;
                    if (enchantment == null) return subResult;
                    int nextLevel = getResultEnchantmentLevel(subResult, entry, enchantment, rune);
                    EnchantmentHelper.apply(subResult, builder -> builder.add(entry, nextLevel));
                    return subResult;
                });

        if (result.isDamageable()) {
            //TODO: make max durability lost dependent on flux
            result.set(DataComponentTypes.MAX_DAMAGE, result.getMaxDamage() - 1);
        }

        return result;
    }

    public static int getResultEnchantmentLevel(ItemStack itemStack, RegistryEntry<Enchantment> entry, Enchantment enchantment, ItemStack rune) {
        if (rune.get(ModItems.OPEN) == null) {
            return 1;
        }
        int currentLevel = EnchantmentHelper.getEnchantments(itemStack).getLevel(entry);
        return Math.min(currentLevel + 1, enchantment.getMaxLevel());
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
}

