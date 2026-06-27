package lunar.tinkerer;

import com.google.common.collect.Streams;
import lunar.tinkerer.enchantingTable.ModEnchantmentScreenHandler;
import lunar.tinkerer.util.Tuple;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

@Environment(value=EnvType.CLIENT)
public class ModEnchantmentScreen
        extends AbstractRecipeBookScreen<ModEnchantmentScreenHandler> {
    protected int backgroundWidth = 176;
    protected int backgroundHeight = 212;
    private static final List<Identifier> CONDUIT_TEXTURES = Stream.of(
            "container/slot/chestplate",
            "container/slot/helmet",
            "container/slot/leggings",
            "container/slot/boots",
            "container/slot/axe",
            "container/slot/pickaxe",
            "container/slot/hoe",
            "container/slot/shield",
            "container/slot/shovel",
            "container/slot/sword",
            "container/slot/lapis_lazuli"
        )
        .map(Identifier::withDefaultNamespace)
        .toList();
    private final CyclingSlotBackground slotIcon = new CyclingSlotBackground(1);

    private static final Identifier TEXTURE = MagicRevamped.id("textures/gui/enchanting_table.png");

    public ModEnchantmentScreen(ModEnchantmentScreenHandler handler, Inventory inventory, Component title) {
        super(handler, new EnchantmentBookComponent(handler), inventory, title);
    }

    @Override
    protected void init() {
        titleLabelY = -18;
        inventoryLabelY = 79;
        super.init();
    }

    @Override
    protected @NonNull ScreenPosition getRecipeBookButtonPosition() {
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
        FormattedText riskLabel = EnchantmentNames.getInstance().getRandomName(this.font, 46 - this.font.width(risk));

        context.fill(RenderPipelines.GUI, x, y, x + 50, y + 14, 0x4F000000);
        context.textWithWordWrap(this.font, riskLabel, x + 3, y + 3, 40, color, true);
        context.text(this.font, Component.literal(risk), x + 3 + this.font.width(riskLabel), y + 3, color);
    }

    public static class EnchantmentBookComponent extends RecipeBookComponent<ModEnchantmentScreenHandler> {
        private static final WidgetSprites TEXTURES = new WidgetSprites(Identifier.withDefaultNamespace("recipe_book/filter_enabled"), Identifier.withDefaultNamespace("recipe_book/filter_disabled"), Identifier.withDefaultNamespace("recipe_book/filter_enabled_highlighted"), Identifier.withDefaultNamespace("recipe_book/filter_disabled_highlighted"));
        private static final Component TOGGLE_CRAFTABLE_TEXT = Component.translatable("gui.recipebook.toggleRecipes.craftable");

        public EnchantmentBookComponent(ModEnchantmentScreenHandler menu) {
            var tabInfo = new RecipeBookComponent.TabInfo(Items.ENCHANTING_TABLE, MagicRevamped.RecipeBookCategories.ENCHANTMENT_RECIPE_BOOK_CATEGORY);
            super(menu, List.of(tabInfo));
        }

        @Override
        protected @NonNull WidgetSprites getFilterButtonTextures() {
            return TEXTURES;
        }

        @Override
        protected boolean isCraftingSlot(@NonNull Slot slot) {
            return false;
        }

        @Override
        protected @NonNull Component getRecipeFilterName() {
            return TOGGLE_CRAFTABLE_TEXT;
        }

        @Override
        protected void selectMatchingRecipes(RecipeCollection recipeCollection, @NonNull StackedItemContents stackedItemContents) {
            recipeCollection.selectRecipes(stackedItemContents, (_) -> true);
        }

        @Override
        protected void fillGhostRecipe(@NonNull GhostSlots ghostSlots, @Nullable RecipeDisplay display, @NonNull ContextMap context) {
            if (display == null) return;
            ghostSlots.setResult(this.menu.getOutputSlot(), context, display.result());
            if (!(display instanceof ShapedCraftingRecipeDisplay enchantmentRecipeDisplay)) return;

            Streams.zip(this.menu.getInputSlots().stream(), enchantmentRecipeDisplay.ingredients().stream(), Tuple::new)
                .forEach(tuple -> ghostSlots.setInput(tuple.getA(), context, tuple.getB()));
        }
    }
}

