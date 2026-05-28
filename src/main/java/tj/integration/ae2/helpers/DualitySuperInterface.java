package tj.integration.ae2.helpers;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngInternalAEInventory;
import tj.integration.ae2.inventory.TJAppEngNetworkInventory;
import appeng.util.inv.InvOperation;
import gregtech.api.util.GTLog;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class DualitySuperInterface extends DualityInterface {

    public DualitySuperInterface(AENetworkProxy networkProxy, IInterfaceHost ih) {
        super(networkProxy, ih);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new IAEItemStack[18], "requireWork");
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new AppEngInternalAEInventory(this, 18, 1024), "config");
        try {
            Field mySource = ObfuscationReflectionHelper.findField(DualityInterface.class, "mySource");
            ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this, new TJAppEngNetworkInventory(() -> {
                try {
                    return networkProxy.getStorage();
                } catch (GridAccessException e) {
                    return null;
                }
            }, (IActionSource) mySource.get(this), this, 18, 1024), "storage");
        } catch (IllegalAccessException e) {
            GTLog.logger.error("Error when trying to reflect on class {} for field storage", DualityInterface.class.getName());
        }
    }

    @Override
    public IAEItemStack injectCraftedItems(final ICraftingLink link, final IAEItemStack acquired, final Actionable mode) {
        return ((IDualitySuperInterface) this).injectTheCraftedItems(link, acquired, mode);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return ((IDualitySuperInterface) this).tickingTheRequest(node, ticksSinceLastCall);
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        ((IDualitySuperInterface) this).onChangeTheInventory(inv, slot, mc, removed, added);
    }
}
