package tj.integration.appeng.helpers;

import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngInternalAEInventory;
import gregtech.api.util.GTLog;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.integration.appeng.tile.inventory.TJAppEngNetworkInventory;

import java.lang.reflect.Field;

public class TJDualityInterface extends DualityInterface {

    public TJDualityInterface(AENetworkProxy networkProxy, IInterfaceHost ih) {
        super(networkProxy, ih);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new IAEItemStack[18], "requireWork");
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new AppEngInternalAEInventory(this, 18, 1024), "config");
        try {
            Field mySource = ObfuscationReflectionHelper.findField(DualityInterface.class, "mySource");
            ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new TJAppEngNetworkInventory(() -> {
                try {
                    return networkProxy.getStorage();
                } catch (GridAccessException e) {
                    throw new RuntimeException(e);
                }
            }, (IActionSource) mySource.get(this), this, 18, 1024), "storage");
        } catch (IllegalAccessException e) {
            GTLog.logger.error("Error when trying to reflect on class {} for field storage", DualityInterface.class.getName());
        }
    }
}
