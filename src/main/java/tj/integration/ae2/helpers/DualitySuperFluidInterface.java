package tj.integration.ae2.helpers;

import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.AEFluidInventory;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import gregtech.api.util.GTLog;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.integration.ae2.inventory.TJAENetworkFluidInventory;

import java.lang.reflect.Field;

public class DualitySuperFluidInterface extends DualityFluidInterface {

    public DualitySuperFluidInterface(AENetworkProxy networkProxy, IFluidInterfaceHost ih) {
        super(networkProxy, ih);
        ObfuscationReflectionHelper.setPrivateValue(DualityFluidInterface.class, this, new IAEFluidStack[18], "requireWork");
        ObfuscationReflectionHelper.setPrivateValue(DualityFluidInterface.class, this, new AEFluidInventory(this, 18), "config");
        try {
            Field mySource = ObfuscationReflectionHelper.findField(DualityFluidInterface.class, "mySource");
            ObfuscationReflectionHelper.setPrivateValue(DualityFluidInterface.class, this, new TJAENetworkFluidInventory(() -> {
                try {
                    return networkProxy.getStorage();
                } catch (GridAccessException e) {
                    return null;
                }
            }, (IActionSource) mySource.get(this), this, 18, 64000), "tanks");
        } catch (IllegalAccessException e) {
            GTLog.logger.error("Error when trying to reflect on class {} for field storage", DualityFluidInterface.class.getName());
        }
    }
}
