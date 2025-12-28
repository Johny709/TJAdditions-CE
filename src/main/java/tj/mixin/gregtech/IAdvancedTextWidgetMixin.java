package tj.mixin.gregtech;

import gregtech.api.gui.widgets.AdvancedTextWidget;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = AdvancedTextWidget.class, remap = false)
public interface IAdvancedTextWidgetMixin {

    @Accessor("displayText")
    List<ITextComponent> getDisplayText();

    @Accessor("displayText")
    void setDisplayText(List<ITextComponent> displayText);

}
