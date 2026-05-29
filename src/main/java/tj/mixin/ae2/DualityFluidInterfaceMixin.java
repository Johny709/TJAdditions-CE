package tj.mixin.ae2;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.integration.ae2.helpers.IDualitySuperFluidInterface;

@Mixin(value = DualityFluidInterface.class, remap = false)
public abstract class DualityFluidInterfaceMixin implements IDualitySuperFluidInterface {

    @Shadow
    @Final
    private AEFluidInventory tanks;

    @Shadow
    private int isWorking;

    @Shadow
    @Final
    private AEFluidInventory config;

    @Shadow
    protected abstract boolean hasConfig();

    @Shadow
    private boolean hasConfig;

    @Shadow
    private boolean resetConfigCache;

    @Shadow
    public abstract void notifyNeighbors();

    @Shadow
    public abstract void saveChanges();

    @Shadow
    protected abstract boolean hasWorkToDo();

    @Shadow
    @Final
    private AENetworkProxy gridProxy;

    @Shadow
    @Final
    private IAEFluidStack[] requireWork;

    @Shadow
    @Final
    private UpgradeInventory upgrades;

    @Shadow
    public abstract int getInstalledUpgrades(Upgrades u);

    @Shadow
    @Final
    private IActionSource interfaceRequestSource;

    @Override
    public TickRateModulation tickingTheRequest(IGridNode node, int tickSinceLastCall) {
        if (!this.gridProxy.isActive()) {
            return TickRateModulation.SLEEP;
        }

        final boolean couldDoWork = this.updateTheStorage();
        return this.hasWorkToDo() ? (couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER) : TickRateModulation.SLEEP;
    }

    @Override
    public void readTheConfig() {
        this.hasConfig = false;

        for (int i = 0; i < this.config.getSlots(); i++) {
            if (this.config.getFluidInSlot(i) != null) {
                this.hasConfig = true;
                break;
            }
        }

        final boolean had = this.hasWorkToDo();

        for (int x = 0; x < this.config.getSlots(); x++) {
            this.updateThePlan(x);
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
                // :P
            }
        }

        this.notifyNeighbors();
    }

    @Override
    public boolean updateTheStorage() {
        boolean didSomething = false;
        for (int x = 0; x < this.config.getSlots(); x++) {
            if (this.requireWork[x] != null) {
                didSomething = this.useThePlan(x) || didSomething;
            }
        }
        return didSomething;
    }

    @Override
    public void updateThePlan(int slot) {
        final IAEFluidStack req = this.config.getFluidInSlot(slot);
        final IAEFluidStack stored = this.tanks.getFluidInSlot(slot);

        if (req == null && (stored != null && stored.getStackSize() > 0)) {
            final IAEFluidStack work = stored.copy();
            this.requireWork[slot] = work.setStackSize(-work.getStackSize());
            return;
        } else if (req != null) {
            final int tankSize = 64000 << this.getInstalledUpgrades(Upgrades.CAPACITY) * 2;
            if (stored == null || stored.getStackSize() == 0) { // need to add stuff!
                this.requireWork[slot] = req.copy();
                this.requireWork[slot].setStackSize(tankSize);
                return;
            } else if (req.equals(stored)) { // same type ( qty different? )!
                if (stored.getStackSize() != tankSize) {
                    this.requireWork[slot] = req.copy();
                    this.requireWork[slot].setStackSize(tankSize - stored.getStackSize());
                    return;
                }
            } else { // Stored != null; dispose!
                final IAEFluidStack work = stored.copy();
                this.requireWork[slot] = work.setStackSize(-work.getStackSize());
                return;
            }
        }

        this.requireWork[slot] = null;
    }

    @Override
    public boolean useThePlan(int slot) {
        IAEFluidStack work = this.requireWork[slot];
        this.isWorking = slot;

        boolean changed = false;
        try {
            final IMEInventory<IAEFluidStack> dest = this.gridProxy.getStorage()
                    .getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
            final IEnergySource src = this.gridProxy.getEnergy();

            if (work.getStackSize() > 0) {
                // make sure strange things didn't happen...
                if (this.tanks.fill(slot, work.getFluidStack(), false) != work.getStackSize()) {
                    changed = true;
                } else if (this.gridProxy.getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)).getStorageList().findPrecise(work) != null) {
                    final IAEFluidStack acquired = Platform.poweredExtraction(src, dest, work, this.interfaceRequestSource);
                    if (acquired != null) {
                        changed = true;
                        final int filled = this.tanks.fill(slot, acquired.getFluidStack(), true);
                        if (filled != acquired.getStackSize()) {
                            throw new IllegalStateException("bad attempt at managing tanks. ( fill )");
                        }
                    }
                }
            } else if (work.getStackSize() < 0) {
                IAEFluidStack toStore = work.copy();
                toStore.setStackSize(-toStore.getStackSize());

                // make sure strange things didn't happen...
                final FluidStack canExtract = this.tanks.drain(slot, toStore.getFluidStack(), false);
                if (canExtract == null || canExtract.amount != toStore.getStackSize()) {
                    changed = true;
                } else {
                    IAEFluidStack notStored = Platform.poweredInsert(src, dest, toStore, this.interfaceRequestSource);
                    toStore.setStackSize(toStore.getStackSize() - (notStored == null ? 0 : notStored.getStackSize()));

                    if (toStore.getStackSize() > 0) {
                        // extract items!
                        changed = true;
                        final FluidStack removed = this.tanks.drain(slot, toStore.getFluidStack(), true);
                        if (removed == null || toStore.getStackSize() != removed.amount) {
                            throw new IllegalStateException("bad attempt at managing tanks. ( drain )");
                        }
                    }
                }
            }
        } catch (final GridAccessException e) {
            // :P
        }

        if (changed) {
            this.updateThePlan(slot);
        }

        this.isWorking = -1;
        return changed;
    }

    @Override
    public void onFluidInventoryHasChanged(IAEFluidTank inventory, FluidStack added, FluidStack removed) {
        if (inventory == this.tanks) {
//            if (added != null) {
//                this.iHost.onStackReturnNetwork(AEFluidStack.fromFluidStack(added));
//            }
            this.saveChanges();
        }
    }

    @Override
    public void onFluidInventoryHasChanged(IAEFluidTank inventory, int slot, InvOperation operation, FluidStack added, FluidStack removed) {
        if (this.isWorking == slot) {
            return;
        }

        if (inventory == this.config) {
            final boolean cfg = this.hasConfig();
            this.readTheConfig();
            if (cfg != this.hasConfig) {
                this.resetConfigCache = true;
                this.notifyNeighbors();
            }
        } else if (inventory == this.tanks) {
//            if (added != null) {
//                this.iHost.onStackReturnNetwork(AEFluidStack.fromFluidStack(added));
//            }
            this.saveChanges();

            final boolean had = this.hasWorkToDo();

            this.updateThePlan(slot);

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
    public void onChangeTheInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
        if (inv == this.upgrades) {
            this.tanks.setCapacity(64000 << this.getInstalledUpgrades(Upgrades.CAPACITY) * 2);
            try {
                this.gridProxy.getTick().alertDevice(this.gridProxy.getNode());
            } catch (GridAccessException ignored) {
            }
            for (int x = 0; x < this.config.getSlots(); x++) {
                this.updateThePlan(x);
            }
        }
    }

    @Override
    public void serializeToNBT(NBTTagCompound data) {
        data.setInteger("capacity", this.tanks.getTankProperties()[0].getCapacity());
    }

    @Override
    public void deserializeFromNBT(NBTTagCompound data) {
        this.tanks.setCapacity(data.getInteger("capacity"));
    }
}
