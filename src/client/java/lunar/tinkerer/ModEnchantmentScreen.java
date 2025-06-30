package lunar.tinkerer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.CyclingSlotIcon;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.GhostRecipe;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.screen.AbstractCraftingScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextParameterMap;

import java.util.List;

@Environment(value=EnvType.CLIENT)
public class ModEnchantmentScreen
        extends RecipeBookScreen<ModEnchantmentScreenHandler> {
    protected int backgroundWidth = 176;
    protected int backgroundHeight = 196;
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

    private static final Identifier TEXTURE = Identifier.of(MagicRevamped.MOD_ID, "textures/gui/enchanting_table.png");

    public ModEnchantmentScreen(ModEnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        //TODO: Make our own widget
        //TODO: Make our own RecipeBookCategory
        super(handler, new RecipeBookWidget<>(handler, List.of(
                new RecipeBookWidget.Tab(Items.LAVA_BUCKET, Items.APPLE, RecipeBookCategories.CRAFTING_MISC)
        )) {
            private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/filter_enabled"), Identifier.ofVanilla((String)"recipe_book/filter_disabled"), Identifier.ofVanilla((String)"recipe_book/filter_enabled_highlighted"), Identifier.ofVanilla((String)"recipe_book/filter_disabled_highlighted"));
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

            }
        }, inventory, title);
    }

    @Override
    protected void init() {
        titleY = -9;
        playerInventoryTitleY = 88;
        super.init();
    }

    @Override
    protected ScreenPos getRecipeBookButtonPos() {
        return new ScreenPos(this.x + 149, this.height / 2 - 5);
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
    }
}

