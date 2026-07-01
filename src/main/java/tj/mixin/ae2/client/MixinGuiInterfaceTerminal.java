package tj.mixin.ae2.client;

import appeng.client.gui.implementations.GuiInterfaceTerminal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GuiInterfaceTerminal.class, remap = false)
public abstract class MixinGuiInterfaceTerminal {

    @ModifyArg(method = "getById", at = @At(value = "INVOKE", target = "Lappeng/client/me/ClientDCInternalInv;<init>(IJJLjava/lang/String;)V"))
    private int modifyArgGetById(int size) {
        return 288; // Max pattern slot count of ME Pattern Interface. The Interface with the most amount of pattern slots.
    }
}
