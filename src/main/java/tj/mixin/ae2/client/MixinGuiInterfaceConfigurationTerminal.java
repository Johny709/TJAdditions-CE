package tj.mixin.ae2.client;

import appeng.client.gui.implementations.GuiInterfaceConfigurationTerminal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GuiInterfaceConfigurationTerminal.class, remap = false)
public abstract class MixinGuiInterfaceConfigurationTerminal {

    @ModifyArg(method = "getById", at = @At(value = "INVOKE", target = "Lappeng/client/me/ClientDCInternalInv;<init>(IJJLjava/lang/String;I)V"), index = 0)
    private int modifyArgGetById(int size) {
        return 36; // Max item config slot count of ME Stocking Interface. The Interface with the most amount of item config slots.
    }
}
