package tj.mixin.ae2;

import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.AdaptorItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tj.integration.appeng.tile.inventory.TJAppEngNetworkInventory;
import tj.integration.appeng.tile.misc.PartSuperInterface;
import tj.integration.appeng.tile.misc.TileSuperInterface;

@Mixin(value = DualityInterface.class, remap = false)
public abstract class DualityInterfaceMixin {

    @Shadow
    @Final
    private AppEngInternalInventory storage;

    @Unique
    private boolean isTJInterface;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void tjInject_Init(AENetworkProxy networkProxy, IInterfaceHost ih, CallbackInfo ci) {
        if (ih instanceof TileSuperInterface || ih instanceof PartSuperInterface)
            this.isTJInterface = true;
    }

    @Inject(method = "getAdaptor", at = @At("HEAD"), cancellable = true)
    private void tjInject_M_GetAdaptor(int slot, CallbackInfoReturnable<InventoryAdaptor> cir) {
        if (this.isTJInterface)
            cir.setReturnValue(new AdaptorItemHandler(((TJAppEngNetworkInventory) this.storage).getBufferWrapper(slot)));
    }
}
