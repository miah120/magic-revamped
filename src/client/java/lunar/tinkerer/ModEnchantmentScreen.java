package lunar.tinkerer;

import java.util.List;

import com.mojang.datafixers.DataFixer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.ingame.CyclingSlotIcon;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.AbstractCraftingRecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
        super(handler, new AbstractCraftingRecipeBookWidget(handler), inventory, title);
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

