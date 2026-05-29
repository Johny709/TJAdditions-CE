package tj.integration.ae2.helpers;

import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.Api;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import gregtech.api.util.GTLog;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import tj.integration.ae2.inventory.TJAENetworkFluidInventory;
import tj.items.item.TJItems;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class DualitySuperFluidInterface extends DualityFluidInterface {

    public DualitySuperFluidInterface(AENetworkProxy networkProxy, IFluidInterfaceHost ih) {
        super(networkProxy, ih);
        ObfuscationReflectionHelper.setPrivateValue(DualityFluidInterface.class, this, new IAEFluidStack[18], "requireWork");
        ObfuscationReflectionHelper.setPrivateValue(DualityFluidInterface.class, this, new AEFluidInventory(this, 18), "config");
        ObfuscationReflectionHelper.setPrivateValue(DualityFluidInterface.class, this, new DualityFluidUpgradeInventory(this, 4), "upgrades");
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

    private static class DualityFluidUpgradeInventory extends UpgradeInventory {

        private int installed;

        public DualityFluidUpgradeInventory(IAEAppEngInventory parent, int s) {
            super(parent, s);
            this.setFilter(new DualityFluidFilter());
        }

        @Override
        public int getMaxInstalled(Upgrades upgrades) {
            return this.installed;
        }

        @Override
        public int getInstalledUpgrades(Upgrades u) {
            return this.installed;
        }

        @Override
        protected void onContentsChanged(int slot) {
            this.installed = 0;
            final ItemStack capacityUpgrade = Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY);
            for (int i = 0; i < this.getSlots(); i++) {
                final ItemStack stack = this.getStackInSlot(i);
                if (stack.isItemEqual(capacityUpgrade)) {
                    this.installed++;
                }
                if (stack.isItemEqual(new ItemStack(TJItems.MAX_CAPACITY_UPGRADE))) {
                    this.installed = 16;
                }
            }
            super.onContentsChanged(slot);
        }

        @Override
        public NBTTagCompound serializeNBT() {
            final NBTTagCompound compound = super.serializeNBT();
            compound.setInteger("installed", this.installed);
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            this.installed = nbt.getInteger("installed");
        }
    }

    private static class DualityFluidFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(IItemHandler iItemHandler, int i, int i1) {
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler iItemHandler, int i, ItemStack itemStack) {
            return itemStack.isItemEqual(Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY)) ||
                    itemStack.isItemEqual(new ItemStack(TJItems.MAX_CAPACITY_UPGRADE));
        }
    }
}
