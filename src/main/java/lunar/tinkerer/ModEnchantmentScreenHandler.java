package lunar.tinkerer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractCraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ModEnchantmentScreenHandler
        extends AbstractCraftingScreenHandler {
    private final ScreenHandlerContext context;
    private final PlayerEntity player;
    private boolean filling;

    public ModEnchantmentScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public ModEnchantmentScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModBlockEntities.ENCHANTMENT_SCREEN_HANDLER, syncId, 3, 3);
        this.context = context;
        this.player = playerInventory.player;

        this.addResultSlot(this.getPlayer(), 127, 32);
        this.addInputSlots(44,32);
        this.addPlayerSlots(playerInventory, 8, 99);
    }

    @Override
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

    protected static void updateResult(ScreenHandler handler, ServerWorld world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, @Nullable RecipeEntry<CraftingRecipe> recipe) {
        ItemStack conduit = craftingInventory.getStack(0);
        if (conduit.isOf(Items.LAPIS_LAZULI)) {
            MagicRevamped.LOGGER.info("Rune Carving!");
            carveRune(handler, world, player, craftingInventory, resultInventory, recipe);
        } else if (conduit.isOf(Items.BOOK)) {
            MagicRevamped.LOGGER.info("Inscribing!");
        } else {
            MagicRevamped.LOGGER.info("Enchanting!");
        }
    }

    private static void carveRune(ScreenHandler handler, ServerWorld world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, @Nullable RecipeEntry<CraftingRecipe> recipe) {
        CraftingRecipeInput craftingRecipeInput = craftingInventory.createRecipeInput();
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
        ItemStack itemStack = ItemStack.EMPTY;
        Optional<RecipeEntry<CraftingRecipe>> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingRecipeInput, world, recipe);
        if (optional.isPresent()) {
            ItemStack itemStack2;
            RecipeEntry<CraftingRecipe> recipeEntry = optional.get();
            CraftingRecipe craftingRecipe = recipeEntry.value();
            if (resultInventory.shouldCraftRecipe(serverPlayerEntity, recipeEntry) && (itemStack2 = craftingRecipe.craft(craftingRecipeInput, world.getRegistryManager())).isItemEnabled(world.getEnabledFeatures())) {
                itemStack = itemStack2;
            }
        }
        resultInventory.setStack(0, itemStack);
        handler.setReceivedStack(0, itemStack);
        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack));
    }

    private static void enchant() {}

    @Override
    public void onContentChanged(Inventory inventory) {
        if (this.filling) { return; }
        this.context.run((world, pos) -> {
            if (world instanceof ServerWorld serverWorld) {
                ModEnchantmentScreenHandler.updateResult(this, serverWorld, this.player, this.craftingInventory, this.craftingResultInventory, null);
            }
        });
    }

    @Override
    public void onInputSlotFillStart() {
        this.filling = true;
    }

    @Override
    public void onInputSlotFillFinish(ServerWorld world, RecipeEntry<CraftingRecipe> recipe) {
        this.filling = false;
        ModEnchantmentScreenHandler.updateResult(this, world, this.player, this.craftingInventory, this.craftingResultInventory, recipe);
    }

    @Override
    public Slot getOutputSlot() {
        return this.slots.getFirst();
    }

    @Override
    public List<Slot> getInputSlots() {
        return this.slots.subList(1, 10);
    }

    @Override
    protected PlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public RecipeBookType getCategory() {
        return RecipeBookType.CRAFTING;
    }
}

