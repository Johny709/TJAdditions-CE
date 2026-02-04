package appeng.helpers;

import appeng.api.networking.security.IActionSource;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.AEFluidInventory;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import gregtech.api.util.GTLog;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import appeng.fluids.util.TJAENetworkFluidInventory;

import java.lang.reflect.Field;

public class TJDualityFluidInterface extends DualityFluidInterface {
    public TJDualityFluidInterface(AENetworkProxy networkProxy, IFluidInterfaceHost ih) {
        super(networkProxy, ih);
        try {
            Field mySource = ObfuscationReflectionHelper.findField(DualityFluidInterface.class, "mySource");
            ObfuscationReflectionHelper.setPrivateValue(DualityFluidInterface.class, this, new TJAENetworkFluidInventory(() -> {
                try {
                    return networkProxy.getStorage();
                } catch (GridAccessException e) {
                    throw new RuntimeException(e);
                }
            }, (IActionSource) mySource.get(this), this, 18, 16000) {
                @Override
                public void setCapacity(int capacity) {
                    super.setCapacity(capacity * 4);
                }
            }, "tanks");
        } catch (IllegalAccessException e) {
            GTLog.logger.error("Error when trying to reflect on class {} for field tanks", DualityFluidInterface.class.getName());
        }
        ObfuscationReflectionHelper.setPrivateValue(DualityFluidInterface.class, this, new AEFluidInventory(this, 18), "config");
    }
}
