package tj.mixin.jei;

import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.InputHandler;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tj.gui.widgets.IRecipeClickArea;

import java.util.Collections;

@Mixin(value = InputHandler.class, remap = false)
public abstract class InputHandlerMixin {

    @Shadow
    @Final
    private RecipesGui recipesGui;

    @Inject(method = "handleMouseClick", at = @At("TAIL"), cancellable = true)
    private void injectHandleMouseClick(GuiScreen guiScreen, int mouseButton, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (guiScreen instanceof IRecipeClickArea) {
            IRecipeClickArea clickArea = (IRecipeClickArea) guiScreen;
            String uid;
            if ((uid = clickArea.getRecipeUid(mouseX, mouseY, mouseButton)) != null && !uid.isEmpty()) {
                this.recipesGui.showCategories(Collections.singletonList(uid));
                cir.setReturnValue(true);
            }
        }
    }
}
