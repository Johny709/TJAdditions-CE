package tj.mixin.ae2;

import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.AEFluidInventory;
import appeng.me.helpers.AENetworkProxy;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.integration.ae2.tile.misc.TJPartFluidInterface;
import tj.integration.ae2.tile.misc.TJTileFluidInterface;

@Mixin(value = DualityFluidInterface.class, remap = false)
public abstract class DualityFluidInterfaceMixin {

    @Shadow
    @Final
    private AEFluidInventory tanks;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectDualityFluidInterface_Init(AENetworkProxy networkProxy, IFluidInterfaceHost ih, CallbackInfo ci) {
        if (ih instanceof TJPartFluidInterface || ih instanceof TJTileFluidInterface)
            for (int i = 0; i < this.tanks.getSlots(); i++) 
                this.tanks.setCapacity(64000);
    }
}
