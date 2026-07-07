package tj.mixin.gregtech;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractWidgetGroup.class, remap = false)
public interface IMixinAbstractWidgetGroup {

    @Accessor("initialized")
    boolean getInit();

    @Accessor("initialized")
    void setInit(boolean init);
}
