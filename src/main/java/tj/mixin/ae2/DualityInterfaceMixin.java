package tj.mixin.ae2;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.MultiCraftingTracker;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;
import com.google.common.primitives.Ints;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import appeng.helpers.ISuperDualityInterface;
import appeng.tile.inventory.TJAppEngNetworkInventory;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

@Mixin(value = DualityInterface.class, remap = false)
public abstract class DualityInterfaceMixin implements ISuperDualityInterface {

    @Shadow
    @Final
    private AppEngInternalInventory storage;

    @Shadow
    private boolean hasConfig;

    @Shadow
    @Final
    private AppEngInternalAEInventory config;

    @Shadow
    protected abstract void updatePlan(int slot);

    @Shadow
    protected abstract boolean hasWorkToDo();

    @Shadow
    @Final
    private AENetworkProxy gridProxy;

    @Shadow
    public abstract void notifyNeighbors();

    @Shadow
    private int isWorking;

    @Shadow
    protected abstract void updateCraftingList();

    @Shadow
    private boolean resetConfigCache;

    @Shadow
    protected abstract boolean hasConfig();

    @Shadow
    @Final
    private AppEngInternalInventory patterns;

    @Shadow
    @Final
    private IInterfaceHost iHost;

    @Shadow
    protected abstract boolean hasItemsToSend();

    @Shadow
    protected abstract void pushItemsOut(EnumSet<EnumFacing> possibleDirections);

    @Shadow
    protected abstract void pushItemsOut(EnumFacing s);

    @Shadow
    protected abstract boolean hasItemsToSendFacing();

    @Shadow
    private EnumMap<EnumFacing, List<ItemStack>> waitingToSendFacing;

    @Shadow
    @Final
    private IAEItemStack[] requireWork;

    @Shadow
    private IMEInventory<IAEItemStack> destination;
    @Shadow
    @Final
    private IActionSource interfaceRequestSource;

    @Shadow
    protected abstract boolean handleCrafting(int x, InventoryAdaptor d, IAEItemStack itemStack);

    @Shadow
    @Final
    private MultiCraftingTracker craftingTracker;

    @Override
    public TickRateModulation tickingTheRequest(IGridNode node, int tickSinceLastCall) {
        if (!this.gridProxy.isActive()) {
            return TickRateModulation.SLEEP;
        }

        //Previous version might have items saved in this list
        //recover them
        if (this.hasItemsToSend()) {
            this.pushItemsOut(this.iHost.getTargets());
        }

        if (this.hasItemsToSendFacing()) {
            for (EnumFacing enumFacing : this.waitingToSendFacing.keySet()) {
                this.pushItemsOut(enumFacing);
            }
        }

        final boolean couldDoWork = this.updateTheStorage();
        return this.hasWorkToDo() ? (couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER) : TickRateModulation.SLEEP;
    }

    @Override
    public boolean updateTheStorage() {
        boolean didSomething = false;

        for (int x = 0; x < this.requireWork.length; x++) {
            if (this.requireWork[x] != null) {
                didSomething = this.useThePlan(x, this.requireWork[x]) || didSomething;
            }
        }

        return didSomething;
    }

    @Override
    public IAEItemStack injectTheCraftedItems(ICraftingLink link, IAEItemStack acquired, Actionable mode) {
        final int slot = ((IMultiCraftingTrackerMixin) this.craftingTracker).getTheSlot(link);

        if (acquired != null && slot >= 0 && slot <= this.requireWork.length) {
            final InventoryAdaptor adaptor = this.getTheAdaptor(slot);

            if (mode == Actionable.SIMULATE) {
                return AEItemStack.fromItemStack(adaptor.simulateAdd(acquired.createItemStack()));
            } else {
                final IAEItemStack is = AEItemStack.fromItemStack(adaptor.addItems(acquired.createItemStack()));
                this.updatePlan(slot);
                return is;
            }
        }

        return acquired;
    }

    @Override
    public InventoryAdaptor getTheAdaptor(int slot) {
        return new AdaptorItemHandler(((TJAppEngNetworkInventory )this.storage).getBufferWrapper(slot));
    }

    @Override
    public boolean useThePlan(int x, IAEItemStack itemStack) {
        final InventoryAdaptor adaptor = this.getTheAdaptor(x);
        this.isWorking = x;

        boolean changed = false;
        try {
            this.destination = this.gridProxy.getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            final IEnergySource src = this.gridProxy.getEnergy();

            if (itemStack.getStackSize() < 0) {
                IAEItemStack toStore = itemStack.copy();
                toStore.setStackSize(-toStore.getStackSize());

                long diff = toStore.getStackSize();

                // make sure strange things didn't happen...
                // TODO: check if OK
                final ItemStack canExtract = adaptor.simulateRemove((int) diff, toStore.getDefinition(), null);
                if (canExtract.isEmpty()) {
                    changed = true;
                    throw new GridAccessException();
                }

                toStore = Platform.poweredInsert(src, this.destination, toStore, this.interfaceRequestSource);

                if (toStore != null) {
                    diff -= toStore.getStackSize();
                }

                if (diff != 0) {
                    // extract items!
                    changed = true;
                    final ItemStack removed = adaptor.removeItems((int) diff, ItemStack.EMPTY, null);
                    if (removed.isEmpty()) {
                        throw new IllegalStateException("bad attempt at managing inventory. ( removeItems )");
                    }
                }
            }

            if (((IMultiCraftingTrackerMixin) this.craftingTracker).isItBusy(x)) {
                changed = this.handleCrafting(x, adaptor, itemStack) || changed;
            } else if (itemStack.getStackSize() > 0) {
                // make sure strange things didn't happen...

                ItemStack inputStack = itemStack.getCachedItemStack(itemStack.getStackSize());

                ItemStack remaining = adaptor.simulateAdd(inputStack);

                if (!remaining.isEmpty()) {
                    itemStack.setCachedItemStack(remaining);
                    changed = true;
                    throw new GridAccessException();
                }

                IAEItemStack storedStack = this.gridProxy.getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)).getStorageList().findPrecise(itemStack);
                if (storedStack != null) {
                    final IAEItemStack acquired = Platform.poweredExtraction(src, this.destination, itemStack, this.interfaceRequestSource);
                    if (acquired != null) {
                        changed = true;
                        inputStack.setCount(Ints.saturatedCast(acquired.getStackSize()));
                        final ItemStack issue = adaptor.addItems(inputStack);
                        if (!issue.isEmpty()) {
                            throw new IllegalStateException("bad attempt at managing inventory. ( addItems )");
                        }
                    } else if (storedStack.isCraftable()) {
                        itemStack.setCachedItemStack(inputStack);
                        changed = this.handleCrafting(x, adaptor, itemStack) || changed;
                    }
                    if (acquired == null) {
                        itemStack.setCachedItemStack(inputStack);
                    }
                }
            }
            // else wtf?
        } catch (final GridAccessException e) {
            // :P
        }

        if (changed) {
            this.updatePlan(x);
        }

        this.isWorking = -1;
        return changed;
    }

    @Override
    public void onChangeTheInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        if (this.isWorking == slot) {
            return;
        }

        if (inv == this.config && (!removed.isEmpty() || !added.isEmpty())) {
            boolean cfg = this.hasConfig();
            this.readTheConfig();
            if (cfg != hasConfig) {
                this.resetConfigCache = true;
                this.notifyNeighbors();
            }
        } else if (inv == this.patterns && (!removed.isEmpty() || !added.isEmpty())) {
            this.updateCraftingList();
        } else if (inv == this.storage && slot >= 0) {
            if (added != ItemStack.EMPTY){
                this.iHost.onStackReturnNetwork(AEItemStack.fromItemStack(added));
            }
            final boolean had = this.hasWorkToDo();

            this.updatePlan(slot);

            final boolean now = this.hasWorkToDo();

            if (had != now) {
                try {
                    if (now) {
                        this.gridProxy.getTick().alertDevice(this.gridProxy.getNode());
                    } else {
                        this.gridProxy.getTick().sleepDevice(this.gridProxy.getNode());
                    }
                } catch (final GridAccessException e) {
                    // :P
                }
            }
        }
    }

    @Override
    public void readTheConfig() {
        this.hasConfig = false;

        for (final ItemStack p : this.config) {
            if (!p.isEmpty()) {
                this.hasConfig = true;
                break;
            }
        }

        final boolean had = this.hasWorkToDo();

        for (int i = 0; i < this.config.getSlots(); i++) {
            this.updatePlan(i);
        }

        final boolean has = this.hasWorkToDo();

        if (had != has) {
            try {
                if (has) {
                    this.gridProxy.getTick().alertDevice(this.gridProxy.getNode());
                } else {
                    this.gridProxy.getTick().sleepDevice(this.gridProxy.getNode());
                }
            } catch (final GridAccessException e) {
                // ;P
            }
        }
        this.notifyNeighbors();
    }
}
