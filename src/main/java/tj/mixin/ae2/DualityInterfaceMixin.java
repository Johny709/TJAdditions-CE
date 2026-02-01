package tj.mixin.ae2;

import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngInternalInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.integration.ae2.tile.misc.TJPartInterface;
import tj.integration.ae2.tile.misc.TJTileInterface;


@Mixin(value = DualityInterface.class, remap = false)
public abstract class DualityInterfaceMixin {

    @Shadow
    @Final
    private AppEngInternalInventory storage;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectDualityInterface_Init(AENetworkProxy networkProxy, IInterfaceHost ih, CallbackInfo ci) {
        if (ih instanceof TJTileInterface || ih instanceof TJPartInterface)
            for (int i = 0; i < this.storage.getSlots(); i++)
                this.storage.setMaxStackSize(i, 1024);
    }
}
