package tj.capability;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.util.function.BooleanConsumer;
import gregtech.common.ConfigHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public abstract class AbstractFuelRecipeLogic<R extends AbstractFuelRecipeLogic<R>> extends FuelRecipeLogic implements IWorkable {

    protected Supplier<IItemHandlerModifiable> importItemsSupplier;
    protected Supplier<IItemHandlerModifiable> exportItemsSupplier;
    protected Supplier<IFluidHandler> exportFluidsSupplier;
    protected Supplier<IEnergyContainer> importEnergySupplier;
    protected IntFunction<IItemHandlerModifiable> inputBus;
    protected LongSupplier maxVoltageSupplier;
    protected IntSupplier tierSupplier;
    protected IntSupplier parallelSupplier;
    private BooleanConsumer activeConsumer;
    protected boolean isWorking = true;
    protected boolean isActive;
    protected boolean wasActiveAndNeedsUpdate;
    protected boolean isDistinct;
    protected boolean hasProblem;
    protected boolean resetEnergy = true;
    protected long energyPerTick;
    protected int progress;
    protected int maxProgress;
    protected int lastInputIndex;
    protected int busCount;
    protected int sleepTimer;
    protected int sleepTime = 1;
    protected int failCount;

    public AbstractFuelRecipeLogic(MetaTileEntity metaTileEntity, FuelRecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer, Supplier<IMultipleTankHandler> fluidTank, long maxVoltage) {
        super(metaTileEntity, recipeMap, energyContainer, fluidTank, maxVoltage);
        this.maxVoltageSupplier = this::getMaxVoltage;
    }

    /**
     * this should be called to initialize some stuff before this workable handler starts running!
     * @param busCount amount of item input buses
     */
    public R initialize(int busCount) {
        this.lastInputIndex = 0;
        this.busCount = busCount;
        return (R) this;
    }

    /**
     * @param importItemsSupplier Item input supplier
     */
    public R setImportItemsSupplier(Supplier<IItemHandlerModifiable> importItemsSupplier) {
        this.importItemsSupplier = importItemsSupplier;
        return (R) this;
    }

    /**
     * @param exportItemsSupplier Item output supplier
     */
    public R setExportItemsSupplier(Supplier<IItemHandlerModifiable> exportItemsSupplier) {
        this.exportItemsSupplier = exportItemsSupplier;
        return (R) this;
    }

    /**
     * @param exportFluidsSupplier Fluid output supplier
     */
    public R setExportFluidsSupplier(Supplier<IFluidHandler> exportFluidsSupplier) {
        this.exportFluidsSupplier = exportFluidsSupplier;
        return (R) this;
    }

    /**
     * @param resetEnergy toggle to reset energy consumption when recipe is done.
     */
    public R setResetEnergy(boolean resetEnergy) {
        this.resetEnergy = resetEnergy;
        return (R) this;
    }

    /**
     * @param importEnergySupplier Energy Input supplier
     */
    public R setImportEnergySupplier(Supplier<IEnergyContainer> importEnergySupplier) {
        this.importEnergySupplier = importEnergySupplier;
        return (R) this;
    }

    /**
     * @param inputBus For Input bus distinct mode
     */
    public R setInputBus(IntFunction<IItemHandlerModifiable> inputBus) {
        this.inputBus = inputBus;
        return (R) this;
    }

    /**
     * @param maxVoltageSupplier Voltage long supplier
     */
    public R setMaxVoltageSupplier(LongSupplier maxVoltageSupplier) {
        this.maxVoltageSupplier = maxVoltageSupplier;
        return (R) this;
    }

    /**
     * @param tierSupplier tier int supplier
     */
    public R setTierSupplier(IntSupplier tierSupplier) {
        this.tierSupplier = tierSupplier;
        return (R) this;
    }

    /**
     * @param parallelSupplier amount of parallels int supplier
     */
    public R setParallelSupplier(IntSupplier parallelSupplier) {
        this.parallelSupplier = parallelSupplier;
        return (R) this;
    }

    /**
     * @param activeConsumer isActive boolean consumer
     */
    public R setActive(BooleanConsumer activeConsumer) {
        this.activeConsumer = activeConsumer;
        return (R) this;
    }

    @Override
    public void update() {
        if (this.metaTileEntity.getWorld().isRemote) return;
        if (!this.isWorking) {
            this.stopRecipe();
            return;
        }
        if (this.wasActiveAndNeedsUpdate && this.isActive)
            this.setActive(false);
        if (this.progress > this.maxProgress) {
            if (this.completeRecipe()) {
                this.progress = 0;
                if (this.resetEnergy)
                    this.energyPerTick = 0;
                if (this.hasProblem)
                    this.setProblem(false);
            } else {
                this.progress--;
                if (!this.hasProblem)
                    this.setProblem(true);
            }
        }
        if (this.progress < 1) {
            if (this.sleepTimer > 1) {
                this.sleepRecipe();
                return;
            }
            boolean canStart = this.startRecipe();
            if (canStart) {
                this.sleepTime = 1;
                this.progressRecipe(this.progress);
                if (!this.isActive)
                    this.setActive(true);
            } else this.failRecipe();
            this.wasActiveAndNeedsUpdate = !canStart;
        } else this.progressRecipe(this.progress);
    }

    /**
     * @return true if the recipe can start
     */
    protected boolean startRecipe() {
        return false;
    }

    /**
     * For every tick the workable handler is on sleep timer
     */
    @OverridingMethodsMustInvokeSuper
    protected void sleepRecipe() {
        this.sleepTimer--;
    }

    /**
     * For every tick the workable handler is stopped
     */
    protected void stopRecipe() {}

    /**
     * When recipe was unable to start. Recommended to invoke super method to utilize sleep timer for performance
     */
    @OverridingMethodsMustInvokeSuper
    protected void failRecipe() {
        if (this.failCount > 4) {
            this.sleepTime = Math.min(this.sleepTime * 2, Math.min(ConfigHolder.maxSleepTime, 400));
            this.failCount = 0;
        } else this.failCount++;
        this.sleepTimer = this.sleepTime;
    }

    protected void progressRecipe(int progress) {
        if (this.importEnergySupplier.get().removeEnergy(this.energyPerTick) == -this.energyPerTick) {
            this.progress++;
        } else if (this.progress > 0)
            this.progress--;
    }

    /**
     * @return true if the recipe can be completed and then will start the next recipe.
     */
    protected boolean completeRecipe() {
        return false;
    }

    protected int calculateOverclock(long baseEnergy, int duration, float multiplier) {
        long voltage = this.maxVoltageSupplier.getAsLong();
        baseEnergy *= 4;
        while (duration > 1 && baseEnergy <= voltage) {
            duration /= multiplier;
            baseEnergy *= 4;
        }
        this.energyPerTick = baseEnergy / 4;
        return Math.max(1, duration);
    }

    public boolean hasEnoughFluid(FluidStack fluid, int amount) {
        FluidStack fluidStack = this.fluidTank.get().drain(fluid, false);
        return fluidStack != null && fluidStack.amount == amount || amount == 0;
    }

    public boolean canOutputFluid(FluidStack fluid, int amount) {
        int fluidStack = this.exportFluidsSupplier.get().fill(fluid, false);
        return fluidStack == amount || amount == 0;
    }

    @Override
    public void receiveCustomData(int id, PacketBuffer buffer) {
        switch (id) {
            case 1: this.isActive = buffer.readBoolean(); break;
            case 2: this.hasProblem = buffer.readBoolean(); break;
            case 3: this.isWorking = buffer.readBoolean(); break;
        }
        this.metaTileEntity.scheduleRenderUpdate();
    }

    @Override
    public void writeInitialData(PacketBuffer buffer) {
        buffer.writeBoolean(this.isActive);
        buffer.writeBoolean(this.hasProblem);
        buffer.writeBoolean(this.isWorking);
    }

    @Override
    public void receiveInitialData(PacketBuffer buffer) {
        this.isActive = buffer.readBoolean();
        this.hasProblem = buffer.readBoolean();
        this.isWorking = buffer.readBoolean();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("progress", this.progress);
        compound.setInteger("maxProgress", this.maxProgress);
        compound.setLong("energyPerTick", this.energyPerTick);
        compound.setBoolean("isWorking", this.isWorking);
        compound.setBoolean("isDistinct", this.isDistinct);
        compound.setBoolean("isActive", this.isActive);
        compound.setBoolean("wasActiveAndNeedsUpdate", this.wasActiveAndNeedsUpdate);
        compound.setBoolean("hasProblem", this.hasProblem);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        this.maxProgress = compound.getInteger("maxProgress");
        this.progress = compound.getInteger("progress");
        this.energyPerTick = compound.getLong("energyPerTick");
        this.isWorking = compound.getBoolean("isWorking");
        this.isDistinct = compound.getBoolean("isDistinct");
        this.isActive = compound.getBoolean("isActive");
        this.wasActiveAndNeedsUpdate = compound.getBoolean("wasActiveAndNeedsUpdate");
        this.hasProblem = compound.getBoolean("hasProblem");
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return capability == GregtechTileCapabilities.CAPABILITY_WORKABLE ? GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this) : null;
    }

    @Override
    public String getName() {
        return "RecipeWorkable";
    }

    @Override
    public int getNetworkID() {
        return TraitNetworkIds.TRAIT_ID_WORKABLE;
    }

    public void setDistinct(boolean distinct) {
        this.isDistinct = distinct;
        this.metaTileEntity.markDirty();
    }

    public boolean isDistinct() {
        return this.isDistinct;
    }

    @Override
    public int getProgress() {
        return this.progress;
    }

    @Override
    public int getMaxProgress() {
        return this.maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
        this.metaTileEntity.markDirty();
    }

    public double getProgressPercent() {
        return this.getMaxProgress() == 0 ? 0.0 : this.getProgress() / (this.getMaxProgress() * 1.0);
    }

    public boolean hasNotEnoughEnergy() {
        return this.isActive && this.importEnergySupplier.get().getEnergyStored() < this.energyPerTick;
    }

    public void setEnergyPerTick(long energyPerTick) {
        this.energyPerTick = energyPerTick;
        this.metaTileEntity.markDirty();
    }

    public long getEnergyPerTick() {
        return this.energyPerTick;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    public boolean hasProblem() {
        return this.hasProblem;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorking;
    }

    @Override
    public void setWorkingEnabled(boolean isWorking) {
        this.isWorking = isWorking;
        if (!this.metaTileEntity.getWorld().isRemote) {
            this.writeCustomData(3, buffer -> buffer.writeBoolean(isWorking));
            this.metaTileEntity.markDirty();
        }
    }

    public void setProblem(boolean hasProblem) {
        this.hasProblem = hasProblem;
        if (!this.metaTileEntity.getWorld().isRemote) {
            this.writeCustomData(2, buffer -> buffer.writeBoolean(hasProblem));
            this.metaTileEntity.markDirty();
        }
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
        if (!this.metaTileEntity.getWorld().isRemote) {
            if (this.activeConsumer != null)
                this.activeConsumer.apply(isActive);
            this.writeCustomData(1, buffer -> buffer.writeBoolean(isActive));
            this.metaTileEntity.markDirty();
        }
    }
}
