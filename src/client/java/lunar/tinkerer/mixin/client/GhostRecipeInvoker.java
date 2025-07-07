package lunar.tinkerer.mixin.client;

import net.minecraft.client.gui.screen.recipebook.GhostRecipe;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.context.ContextParameterMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GhostRecipe.class)
public interface GhostRecipeInvoker {
    @Invoker("addResults")
    void invokeAddResults(Slot slot, ContextParameterMap context, SlotDisplay display);

    @Invoker("addInputs")
    void invokeAddInputs(Slot slot, ContextParameterMap context, SlotDisplay display);
}
