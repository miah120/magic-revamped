package lunar.tinkerer;

import lunar.tinkerer.EnchantmentTable.EnchantingResultSlot;
import lunar.tinkerer.EnchantmentTable.ModEnchantmentScreenHandler;
import lunar.tinkerer.mixin.client.GhostRecipeInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.CyclingSlotIcon;
import net.minecraft.client.gui.screen.ingame.EnchantingPhrases;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.GhostRecipe;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextParameterMap;

import java.util.List;
import java.util.Objects;

@Environment(value=EnvType.CLIENT)
public class ModEnchantmentScreen
        extends RecipeBookScreen<ModEnchantmentScreenHandler> {
    protected int backgroundWidth = 176;
    protected int backgroundHeight = 212;
    private static final List<Identifier> CONDUIT_TEXTURES = List.of(
            Identifier.ofVanilla("container/slot/chestplate"),
            Identifier.ofVanilla("container/slot/helmet"),
            Identifier.ofVanilla("container/slot/leggings"),
            Identifier.ofVanilla("container/slot/boots"),
            Identifier.ofVanilla("container/slot/axe"),
            Identifier.ofVanilla("container/slot/pickaxe"),
            Identifier.ofVanilla("container/slot/hoe"),
            Identifier.ofVanilla("container/slot/shield"),
            Identifier.ofVanilla("container/slot/shovel"),
            Identifier.ofVanilla("container/slot/sword"),
            Identifier.ofVanilla("container/slot/lapis_lazuli")
    );
    private final CyclingSlotIcon slotIcon = new CyclingSlotIcon(1);

    private static final Identifier TEXTURE = MagicRevamped.identifier("textures/gui/enchanting_table.png");

    public ModEnchantmentScreen(ModEnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, new RecipeBookWidget<>(handler, List.of(
                new RecipeBookWidget.Tab(Items.ENCHANTING_TABLE, ModRecipeTypes.ENCHANTMENT_RECIPE_BOOK_CATEGORY)
        )) {
            private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/filter_enabled"), Identifier.ofVanilla("recipe_book/filter_disabled"), Identifier.ofVanilla("recipe_book/filter_enabled_highlighted"), Identifier.ofVanilla("recipe_book/filter_disabled_highlighted"));
            private static final Text TOGGLE_CRAFTABLE_TEXT = Text.translatable("gui.recipebook.toggleRecipes.craftable");

            @Override
            protected boolean isValid(Slot slot) {
                return this.craftingScreenHandler.getOutputSlot() == slot || this.craftingScreenHandler.getInputSlots().contains(slot);
            }

            @Override
            protected void setBookButtonTexture() {
                this.toggleCraftableButton.setTextures(TEXTURES);
            }

            @Override
            protected Text getToggleCraftableButtonText() {
                return TOGGLE_CRAFTABLE_TEXT;
            }

            @Override
            protected void populateRecipes(RecipeResultCollection recipeResultCollection, RecipeFinder recipeFinder) {
                recipeResultCollection.populateRecipes(recipeFinder, (x) -> true);
            }
            @Override
            protected void showGhostRecipe(GhostRecipe ghostRecipe, RecipeDisplay display, ContextParameterMap context) {
                ((GhostRecipeInvoker) ghostRecipe).invokeAddResults(this.craftingScreenHandler.getOutputSlot(), context, display.result());
                Objects.requireNonNull(display);
                EnchantmentRecipeDisplay enchantmentRecipeDisplay = (EnchantmentRecipeDisplay) display;
                List<Slot> list2 = this.craftingScreenHandler.getInputSlots();
                List<SlotDisplay> list = enchantmentRecipeDisplay.ingredients();
                list.addAll(enchantmentRecipeDisplay.specialIngredients());
                int i = Math.min(list.size(), list2.size());
                for (int j = 0; j < i; ++j) {
                    ((GhostRecipeInvoker) ghostRecipe).invokeAddInputs(
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
        titleY = -18;
        playerInventoryTitleY = 79;
        super.init();
    }

    @Override
    protected ScreenPos getRecipeBookButtonPos() {
        return new ScreenPos(this.x + 149, this.height / 2 - 14);
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        this.slotIcon.updateTexture(CONDUIT_TEXTURES);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        assert this.client != null;
        assert this.client.player != null;
        context.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            TEXTURE,
            this.x,
            (this.height - this.backgroundHeight) / 2,
            0.0f,
            0.0f,
            this.backgroundWidth,
            this.backgroundHeight,
            256,
            256
        );
        this.slotIcon.render(this.handler, context, deltaTicks, this.x, this.y);
        this.renderCooldown(context);
        this.renderRisk(context);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
    }

    public void renderCooldown(DrawContext context) {
        int x = this.handler.resultSlot.x + this.x;
        int y = this.handler.resultSlot.y + this.y;
        int timeout = this.handler.resultSlot.timeout;
        context.fill(
                RenderPipelines.GUI,
                x, y + 16 - Math.floorDiv(16 * timeout, EnchantingResultSlot.MAX_TIME_OUT),
                x+16, y+16,
                0xBFBFBFAA
    );
    }

    public void renderRisk(DrawContext context) {
        if (this.handler.resultSlot.inventory.isEmpty()) return;
        int x = this.handler.resultSlot.x + this.x - 33;
        int y = this.handler.resultSlot.y + this.y + 47;
        String risk = ": " + ModEnchantmentScreenHandler.getLevelRequirement(this.handler.craftingInventory);
        EnchantingPhrases.getInstance().setSeed(this.handler.getSeed());
        StringVisitable riskLabel = EnchantingPhrases.getInstance()
            .generatePhrase(
                this.textRenderer,
                46 - this.textRenderer.getWidth(risk)
            );

        context.fill(
            RenderPipelines.GUI,
            x, y,
            x + 50, y + 14,
            0x4F000000
        );

        context.drawWrappedTextWithShadow(
            this.textRenderer,
            riskLabel,
            x + 3,
            y + 3,
            40,
            -8323296
        );
        context.drawTextWithShadow(
            this.textRenderer,
            risk,
            x + 3 + this.textRenderer.getWidth(riskLabel),
            y + 3,
            -8323296
        );
    }

}

