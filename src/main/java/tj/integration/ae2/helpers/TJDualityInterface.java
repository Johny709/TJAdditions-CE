package tj.integration.ae2.helpers;

import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngInternalAEInventory;
import gregtech.api.util.GTLog;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.integration.ae2.tile.inventory.TJAppEngNetworkInventory;

import java.lang.reflect.Field;

public class TJDualityInterface extends DualityInterface {

    public TJDualityInterface(AENetworkProxy networkProxy, IInterfaceHost ih) {
        super(networkProxy, ih);
        Field requireWork = ObfuscationReflectionHelper.findField(DualityInterface.class, "requireWork");
        try {
            requireWork.set(this, new IAEItemStack[18]);
        } catch (IllegalAccessException e) {
            GTLog.logger.error("Error when trying to reflect on class {} for field requireWork", DualityInterface.class.getName());
        }
        Field config = ObfuscationReflectionHelper.findField(DualityInterface.class, "config");
        try {
            config.set(this, new AppEngInternalAEInventory(this, 18, 1024));
        } catch (IllegalAccessException e) {
            GTLog.logger.error("Error when trying to reflect on class {} for field config", DualityInterface.class.getName());
        }
        Field storage = ObfuscationReflectionHelper.findField(DualityInterface.class, "storage");
        Field mySource = ObfuscationReflectionHelper.findField(DualityInterface.class, "mySource");
        try {
            storage.set(this, new TJAppEngNetworkInventory(() -> {
                try {
                    return networkProxy.getStorage();
                } catch (GridAccessException e) {
                    throw new RuntimeException(e);
                }
            }, (IActionSource) mySource.get(this), this, 18, 1024));
        } catch (IllegalAccessException e) {
            GTLog.logger.error("Error when trying to reflect on class {} for field storage", DualityInterface.class.getName());
        }
    }
}
