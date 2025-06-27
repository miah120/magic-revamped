package lunar.tinkerer;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantingPhrases;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class ModEnchantmentScreen
        extends HandledScreen<ModEnchantmentScreenHandler> {
    protected int backgroundWidth = 176;
    protected int backgroundHeight = 186;

    private static final Identifier TEXTURE = Identifier.of(MagicRevamped.MOD_ID, "textures/gui/enchanting_table.png");
    private final Random random = Random.create();
    public float nextPageAngle;
    public float pageAngle;
    public float approximatePageAngle;
    public float pageRotationSpeed;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;
    private ItemStack stack = ItemStack.EMPTY;

    public ModEnchantmentScreen(ModEnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        assert this.client != null;
        assert this.client.player != null;
        this.client.player.experienceBarDisplayStartTime = this.client.player.age;
        this.doTick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        assert this.client != null;
        assert this.client.interactionManager != null;
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        for (int k = 0; k < 3; ++k) {
            double d = mouseX - (double)(i + 60);
            double e = mouseY - (double)(j + 14 + 19 * k);
            if (!(d >= 0.0) || !(e >= 0.0) || !(d < 108.0) || !(e < 19.0) || !this.handler.onButtonClick(this.client.player, k)) continue;
            this.client.interactionManager.clickButton(this.handler.syncId, k);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        assert this.client != null;
        assert this.client.player != null;
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0f, 0.0f, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        assert this.client != null;
        assert this.client.player != null;
        assert this.client.world != null;
        float f = this.client.getRenderTickCounter().getTickProgress(false);
        super.render(context, mouseX, mouseY, f);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        boolean bl = this.client.player.isInCreativeMode();
        int i = this.handler.getLapisCount();
        for (int j = 0; j < 3; ++j) {
            int k = this.handler.enchantmentPower[j];
            Optional<RegistryEntry.Reference<Enchantment>> optional = this.client.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(this.handler.enchantmentId[j]);
            if (optional.isEmpty()) continue;
            int l = this.handler.enchantmentLevel[j];
            int m = j + 1;
            if (!this.isPointWithinBounds(60, 14 + 19 * j, 108, 17, mouseX, mouseY) || k <= 0 || l < 0) continue;
            ArrayList<Text> list = Lists.newArrayList();
            list.add(Text.translatable("container.enchant.clue", Enchantment.getName(optional.get(), l)).formatted(Formatting.WHITE));
            if (!bl) {
                list.add(ScreenTexts.EMPTY);
                if (this.client.player.experienceLevel < k) {
                    list.add(Text.translatable("container.enchant.level.requirement", this.handler.enchantmentPower[j]).formatted(Formatting.RED));
                } else {
                    MutableText mutableText = m == 1 ? Text.translatable("container.enchant.lapis.one") : Text.translatable("container.enchant.lapis.many", m);
                    list.add(mutableText.formatted(i >= m ? Formatting.GRAY : Formatting.RED));
                    MutableText mutableText2 = m == 1 ? Text.translatable("container.enchant.level.one") : Text.translatable("container.enchant.level.many", m);
                    list.add(mutableText2.formatted(Formatting.GRAY));
                }
            }
            context.drawTooltip(this.textRenderer, list, mouseX, mouseY);
            break;
        }
    }

    public void doTick() {
        ItemStack itemStack = this.handler.getSlot(0).getStack();
        if (!ItemStack.areEqual(itemStack, this.stack)) {
            this.stack = itemStack;
            do {
                this.approximatePageAngle += (float)(this.random.nextInt(4) - this.random.nextInt(4));
            } while (this.nextPageAngle <= this.approximatePageAngle + 1.0f && this.nextPageAngle >= this.approximatePageAngle - 1.0f);
        }
        this.pageAngle = this.nextPageAngle;
        this.pageTurningSpeed = this.nextPageTurningSpeed;
        boolean bl = false;
        for (int i = 0; i < 3; ++i) {
            if (this.handler.enchantmentPower[i] == 0) continue;
            bl = true;
        }
        float g = 0.2f;
        this.nextPageTurningSpeed += bl ? g : -g;
        this.nextPageTurningSpeed = MathHelper.clamp(this.nextPageTurningSpeed, 0.0f, 1.0f);
        float f = (this.approximatePageAngle - this.nextPageAngle) * 0.4f;
        f = MathHelper.clamp(f, -g, g);
        this.pageRotationSpeed += (f - this.pageRotationSpeed) * 0.9f;
        this.nextPageAngle += this.pageRotationSpeed;
    }
}

