package tj.mixin.gregtech;

import gregtech.api.gui.ModularUI;
import gregtech.api.gui.impl.ModularUIGui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.gui.widgets.IRecipeClickArea;

@Mixin(value = ModularUIGui.class, remap = false)
public abstract class ModularUIGuiMixin implements IRecipeClickArea {

    @Shadow
    @Final
    private ModularUI modularUI;

    @Override
    public String getRecipeUid(int mouseX, int mouseY, int mouseButton) {
        return this.modularUI.guiWidgets.values().stream()
                .filter(widget -> widget instanceof IRecipeClickArea)
                .findFirst()
                .map(widget -> ((IRecipeClickArea) widget).getRecipeUid(mouseX, mouseY, mouseButton))
                .orElse(null);
    }
}
