package tj.mixin.gregtech;

import gregtech.api.gui.impl.ModularUIGui;
import gregtech.api.gui.impl.ModularUIGuiHandler;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(value = ModularUIGuiHandler.class, remap = false)
public abstract class ModularUIGuiHandlerMixin {

    @Inject(method = "getGuiExtraAreas(Lgregtech/api/gui/impl/ModularUIGui;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void injectGetGuiExtraAreas(ModularUIGui guiContainer, CallbackInfoReturnable<List<Rectangle>> cir) {
        cir.setReturnValue(guiContainer.getModularUI().guiWidgets.values().stream()
                .filter(widget -> widget instanceof IAdvancedGuiHandler<?>)
                .flatMap(widget -> ((IAdvancedGuiHandler<ModularUIGui>) widget).getGuiExtraAreas(guiContainer).stream())
                .collect(Collectors.toList()));
    }
}
