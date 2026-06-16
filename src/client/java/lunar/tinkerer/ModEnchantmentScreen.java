package lunar.tinkerer;

import lunar.tinkerer.enchantingTable.ModEnchantmentScreenHandler;
import lunar.tinkerer.mixin.client.GhostSlotsInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Environment(value=EnvType.CLIENT)
public class ModEnchantmentScreen
        extends AbstractRecipeBookScreen<ModEnchantmentScreenHandler> {
    protected int backgroundWidth = 176;
    protected int backgroundHeight = 212;
    private static final List<Identifier> CONDUIT_TEXTURES = List.of(
            Identifier.withDefaultNamespace("container/slot/chestplate"),
            Identifier.withDefaultNamespace("container/slot/helmet"),
            Identifier.withDefaultNamespace("container/slot/leggings"),
            Identifier.withDefaultNamespace("container/slot/boots"),
            Identifier.withDefaultNamespace("container/slot/axe"),
            Identifier.withDefaultNamespace("container/slot/pickaxe"),
            Identifier.withDefaultNamespace("container/slot/hoe"),
            Identifier.withDefaultNamespace("container/slot/shield"),
            Identifier.withDefaultNamespace("container/slot/shovel"),
            Identifier.withDefaultNamespace("container/slot/sword"),
            Identifier.withDefaultNamespace("container/slot/lapis_lazuli")
    );
    private final CyclingSlotBackground slotIcon = new CyclingSlotBackground(1);

    private static final Identifier TEXTURE = MagicRevamped.identifier("textures/gui/enchanting_table.png");

    public ModEnchantmentScreen(ModEnchantmentScreenHandler handler, Inventory inventory, Component title) {
        super(handler, new RecipeBookComponent<>(handler, List.of(
                new RecipeBookComponent.TabInfo(Items.ENCHANTING_TABLE, ModRecipeTypes.ENCHANTMENT_RECIPE_BOOK_CATEGORY)
        )) {
            private static final WidgetSprites TEXTURES = new WidgetSprites(Identifier.withDefaultNamespace("recipe_book/filter_enabled"), Identifier.withDefaultNamespace("recipe_book/filter_disabled"), Identifier.withDefaultNamespace("recipe_book/filter_enabled_highlighted"), Identifier.withDefaultNamespace("recipe_book/filter_disabled_highlighted"));
            private static final Component TOGGLE_CRAFTABLE_TEXT = Component.translatable("gui.recipebook.toggleRecipes.craftable");

            @Override
            protected WidgetSprites getFilterButtonTextures() {
                return TEXTURES;
            }

            @Override
            protected boolean isCraftingSlot(@NonNull Slot slot) {
                return false;
            }

            @Override
            protected Component getRecipeFilterName() {
                return TOGGLE_CRAFTABLE_TEXT;
            }

            @Override
            protected void selectMatchingRecipes(RecipeCollection recipeCollection, StackedItemContents stackedItemContents) {
                recipeCollection.selectRecipes(stackedItemContents, (x) -> true);
            }

            @Override
            protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay display, ContextMap context) {
                ((GhostSlotsInvoker) ghostSlots).invokeAddResults(this.menu.getOutputSlot(), context, display.result());
                Objects.requireNonNull(display);
                ShapedCraftingRecipeDisplay enchantmentRecipeDisplay = (ShapedCraftingRecipeDisplay) display;
                List<Slot> list2 = this.menu.getInputSlots();
                //Reorder so recipe JSON matches display
                List<SlotDisplay> list = Stream.of(4, 1, 2, 5, 8, 7, 6, 3, 0)
                    .map(i -> i < enchantmentRecipeDisplay.ingredients().size() ? enchantmentRecipeDisplay.ingredients().get(i) : SlotDisplay.Empty.INSTANCE)
                    .toList();
                int i = Math.min(list.size(), list2.size());
                for (int j = 0; j < i; ++j) {
                    ((GhostSlotsInvoker) ghostSlots).invokeAddInputs(
                            list2.get(j),
                            context,
                            list.get(j)
                    );
                }
            }
        }, inventory, title);
    }

    @Override
    protected void init() {
        titleLabelY = -18;
        inventoryLabelY = 79;
        super.init();
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return new ScreenPosition(this.leftPos + 149, this.height / 2 - 14);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.slotIcon.tick(CONDUIT_TEXTURES);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.leftPos,
                (this.height - this.backgroundHeight) / 2,
                0.0f,
                0.0f,
                this.backgroundWidth,
                this.backgroundHeight,
                256,
                256
        );
        this.slotIcon.extractRenderState(this.menu, guiGraphics, f, this.leftPos, this.topPos);
        this.renderCooldown(guiGraphics);
        this.renderRisk(guiGraphics);
    }
    
    public void renderCooldown(GuiGraphicsExtractor context) {
        int x = this.menu.resultSlot.x + this.leftPos;
        int y = this.menu.resultSlot.y + this.topPos;
        int timeout = this.menu.timeout.get();
        context.fill(
            RenderPipelines.GUI,
            x, y + 16 - Math.floorDiv(16 * timeout, ModEnchantmentScreenHandler.MAX_TIME_OUT),
            x+16, y+16,
            0xBFBFBFAA
        );
    }

    public void renderRisk(GuiGraphicsExtractor context) {
        if (this.menu.resultSlot.container.isEmpty()) return;
        int x = this.menu.resultSlot.x + this.leftPos - 33;
        int y = this.menu.resultSlot.y + this.topPos + 47;

        int color = this.menu.resultSlot.mayPickup(this.menu.player) ? -8323296 : 0xDFd31b1b;

        String risk = ": " + ModEnchantmentScreenHandler.getLevelRequirement(this.menu.craftingInventory, this.menu.player.level(), this.menu.getBlockPos());
        EnchantmentNames.getInstance().initSeed(this.menu.getSeed());
        FormattedText riskLabel = EnchantmentNames.getInstance()
            .getRandomName(
                this.font, 
                46 - this.font.width(risk)
            );

        context.fill(
            RenderPipelines.GUI,
            x, y,
            x + 50, y + 14,
            0x4F000000
        );
        context.textWithWordWrap(
            this.font,
            riskLabel,
            x + 3,
            y + 3,
            40,
            color,
            true
        );
        context.text(
            this.font,
            Component.literal(risk),
            x + 3 + this.font.width(riskLabel),
            y + 3,
            color
        );
    }

}

