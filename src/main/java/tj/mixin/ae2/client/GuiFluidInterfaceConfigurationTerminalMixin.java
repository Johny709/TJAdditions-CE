package tj.mixin.ae2.client;

import appeng.client.gui.implementations.GuiFluidInterfaceConfigurationTerminal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GuiFluidInterfaceConfigurationTerminal.class, remap = false)
public abstract class GuiFluidInterfaceConfigurationTerminalMixin {

    @ModifyArg(method = "getById", at = @At(value = "INVOKE", target = "Lappeng/client/me/ClientDCInternalFluidInv;<init>(IJJLjava/lang/String;I)V"), index = 0)
    private int modifyArgGetById(int size) {
        return 36; // Max fluid config slot count of ME Stocking Fluid Interface. The Interface with the most amount of fluid config slots.
    }
}
