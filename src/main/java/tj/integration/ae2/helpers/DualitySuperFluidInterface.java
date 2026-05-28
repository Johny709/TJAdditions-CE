package tj.integration.ae2.helpers;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.inv.InvOperation;
import gregtech.api.util.GTLog;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import tj.integration.ae2.inventory.TJAENetworkFluidInventory;

import javax.annotation.Nonnull;
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

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return ((IDualitySuperFluidInterface) this).tickingTheRequest(node, ticksSinceLastCall);
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        ((IDualitySuperFluidInterface) this).onFluidInventoryHasChanged(inv, slot, null, null, null);
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inventory, int slot, InvOperation operation, FluidStack added, FluidStack removed) {
        ((IDualitySuperFluidInterface) this).onFluidInventoryHasChanged(inventory, slot, operation, added, removed);
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
        ((IDualitySuperFluidInterface) this).onChangeTheInventory(inv, slot, mc, removedStack, newStack);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        ((IDualitySuperFluidInterface) this).serializeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        ((IDualitySuperFluidInterface) this).deserializeFromNBT(data);
        ((IDualitySuperFluidInterface) this).readTheConfig();
    }
}
