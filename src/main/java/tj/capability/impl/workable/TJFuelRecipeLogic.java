package tj.capability.impl.workable;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.FluidFuelInfo;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJValues;
import tj.capability.AbstractFuelRecipeLogic;
import tj.capability.IGeneratorInfo;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;

import java.util.*;
import java.util.function.Supplier;

public class TJFuelRecipeLogic extends AbstractFuelRecipeLogic<TJFuelRecipeLogic> implements IGeneratorInfo, IFuelable, IItemFluidHandlerInfo {

    protected final Set<FluidStack> lastSearchedFluid = new HashSet<>();
    protected final Set<FluidStack> blacklistFluid = new HashSet<>();
    protected final List<FluidStack> fluidInputs = new ArrayList<>();
    protected final List<FluidStack> fluidOutputs = new ArrayList<>();
    private boolean voidEnergy = true;
    protected int consumption;
    private int searchCount;
    protected String fuelName;

    public TJFuelRecipeLogic(MetaTileEntity metaTileEntity, FuelRecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer, Supplier<IMultipleTankHandler> fluidTank, long maxVoltage) {
        super(metaTileEntity, recipeMap, energyContainer, fluidTank, maxVoltage);
    }

    @Override
    protected boolean startRecipe() {
        FluidStack fuelStack = null;
        for (int i = 0; i < this.fluidTank.get().getTanks(); i++) {
            IFluidTank tank = this.fluidTank.get().getTankAt(i);
            FluidStack stack = tank.getFluid();
            if (stack == null) continue;
            if (fuelStack == null) {
                if (this.blacklistFluid.contains(stack) || this.lastSearchedFluid.contains(stack)) continue;
                fuelStack = stack.copy();
                this.lastSearchedFluid.add(fuelStack);
            } else if (fuelStack.isFluidEqual(stack)) {
                long amount = fuelStack.amount + stack.amount;
                fuelStack.amount = (int) Math.min(Integer.MAX_VALUE, amount);
            }
        }
        fuelStack = this.tryAcquireNewRecipe(fuelStack);
        if (fuelStack != null && fuelStack.isFluidStackIdentical(this.fluidTank.get().drain(fuelStack, false))) {
            FluidStack fluidStack = this.fluidTank.get().drain(fuelStack, true);
            this.fuelName = fluidStack.getUnlocalizedName();
            this.lastSearchedFluid.remove(fuelStack);
            this.consumption = fluidStack.amount;
            this.fluidInputs.add(fuelStack);
            return true; //recipe is found and ready to use
        }
        if (++this.searchCount >= this.fluidTank.get().getTanks()) {
            this.lastSearchedFluid.clear();
            this.searchCount = 0;
        }
        return false;
    }

    @Override
    protected void progressRecipe(int progress) {
        if (this.voidEnergy || this.energyContainer.get().getEnergyCanBeInserted() >= this.energyPerTick) {
            this.energyContainer.get().addEnergy(this.energyPerTick);
            if (this.hasProblem)
                this.setProblem(false);
            this.progress++;
        } else if (!this.hasProblem)
            this.setProblem(true);
    }

    @Override
    protected boolean completeRecipe() {
        this.fluidInputs.clear();
        this.fluidOutputs.clear();
        this.lastSearchedFluid.clear();
        return true;
    }

    protected FluidStack tryAcquireNewRecipe(FluidStack fuelStack) {
        FuelRecipe currentRecipe;
        if (this.previousRecipe != null && this.previousRecipe.matches(this.getMaxVoltage(), fuelStack)) {
            //if previous recipe still matches inputs, try to use it
            currentRecipe = this.previousRecipe;
        } else {
            //else, try searching new recipe for given inputs
            currentRecipe = this.recipeMap.findRecipe(this.getMaxVoltage(), fuelStack);
            //if we found recipe that can be buffered, buffer it
            if (currentRecipe != null) {
                this.previousRecipe = currentRecipe;
            } else this.blacklistFluid.add(fuelStack); // blacklist fluid not found in recipe map to prevent search slowdown.
        }
        if (currentRecipe != null && checkRecipe(currentRecipe)) {
            int fuelAmountToUse = this.calculateFuelAmount(currentRecipe);
            if (fuelStack.amount >= fuelAmountToUse) {
                this.maxProgress = this.calculateRecipeDuration(currentRecipe);
                this.energyPerTick = this.startRecipe(currentRecipe, fuelAmountToUse, this.maxProgress);
                FluidStack recipeFluid = currentRecipe.getRecipeFluid();
                recipeFluid.amount = fuelAmountToUse;
                return recipeFluid;
            }
        }
        return null;
    }

    @Override
    public Collection<IFuelInfo> getFuels() {
        final IMultipleTankHandler fluidTanks = this.fluidTank.get();
        if (fluidTanks == null)
            return Collections.emptySet();

        final LinkedHashMap<String, IFuelInfo> fuels = new LinkedHashMap<>();
        // Fuel capacity is all tanks
        int fuelCapacity = 0;
        for (IFluidTank fluidTank : fluidTanks) {
            fuelCapacity += fluidTank.getCapacity();
        }

        for (IFluidTank fluidTank : fluidTanks) {
            final FluidStack tankContents = fluidTank.drain(Integer.MAX_VALUE, false);
            if (tankContents == null || tankContents.amount <= 0) {
                fuelCapacity -= fluidTank.getCapacity();
                continue;
            }
            int fuelRemaining = tankContents.amount;
            FuelRecipe recipe = this.recipeMap.findRecipe(this.maxVoltageSupplier.getAsLong(), tankContents);
            if (recipe == null || this.lastSearchedFluid.contains(recipe.getRecipeFluid())) {
                fuelCapacity -= fluidTank.getCapacity();
                continue;
            }
            int amountPerRecipe = this.calculateFuelAmount(recipe);
            int duration = this.calculateRecipeDuration(recipe);
            long fuelBurnTime = ((long) duration * fuelRemaining) / amountPerRecipe;

            FluidFuelInfo fuelInfo = (FluidFuelInfo) fuels.get(tankContents.getUnlocalizedName());
            if (fuelInfo == null) {
                fuelInfo = new FluidFuelInfo(tankContents, fuelRemaining, fuelCapacity, amountPerRecipe, fuelBurnTime);
                fuels.put(tankContents.getUnlocalizedName(), fuelInfo);
            } else {
                fuelInfo.addFuelRemaining(fuelRemaining);
                fuelInfo.addFuelBurnTime(fuelBurnTime);
            }
        }
        return fuels.values();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        NBTTagList fluidInputList = new NBTTagList(), fluidOutputList = new NBTTagList();
        for (FluidStack stack : this.fluidInputs)
            fluidInputList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        for (FluidStack stack : this.fluidOutputs)
            fluidOutputList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        compound.setTag("fluidInputs", fluidInputList);
        compound.setTag("fluidOutputs", fluidOutputList);
        compound.setInteger("consumption", this.consumption);
        compound.setBoolean("voidEnergy", this.voidEnergy);
        if (this.fuelName != null)
            compound.setString("fuelName", this.fuelName);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        NBTTagList fluidInputList = compound.getTagList("fluidInputs", 10), fluidOutputList = compound.getTagList("fluidOutputs", 10);
        for (int i = 0; i < fluidInputList.tagCount(); i++)
            this.fluidInputs.add(FluidStack.loadFluidStackFromNBT(fluidInputList.getCompoundTagAt(i)));
        for (int i = 0; i < fluidOutputList.tagCount(); i++)
            this.fluidOutputs.add(FluidStack.loadFluidStackFromNBT(fluidOutputList.getCompoundTagAt(i)));
        this.consumption = compound.getInteger("consumption");
        this.voidEnergy = compound.getBoolean("voidEnergy");
        if (compound.hasKey("fuelName"))
            this.fuelName = compound.getString("fuelName");
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_GENERATOR)
            return TJCapabilities.CAPABILITY_GENERATOR.cast(this);
        if (capability == GregtechCapabilities.CAPABILITY_FUELABLE)
            return GregtechCapabilities.CAPABILITY_FUELABLE.cast(this);
        if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
            return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    public FluidStack getFuelStack() {
        if (this.previousRecipe == null)
            return null;
        FluidStack fuelStack = this.previousRecipe.getRecipeFluid();
        return this.fluidTank.get() != null ? this.fluidTank.get().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false) : null;
    }

    public void setVoidEnergy(boolean voidEnergy, String id) {
        this.voidEnergy = voidEnergy;
        this.metaTileEntity.markDirty();
    }

    public boolean isVoidEnergy() {
        return this.voidEnergy;
    }

    public String getFuelName() {
        return this.fuelName;
    }

    public long getEnergyStored() {
        return this.energyContainer.get() != null ? this.energyContainer.get().getEnergyStored() : 0;
    }

    public long getEnergyCapacity() {
        return this.energyContainer.get() != null ? this.energyContainer.get().getEnergyCapacity() : 0;
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return this.fluidInputs;
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return this.fluidOutputs;
    }

    @Override
    public long getProduction() {
        return this.energyPerTick;
    }

    @Override
    public long getConsumption() {
        return this.consumption;
    }

    @Override
    public String[] consumptionInfo() {
        int seconds = this.maxProgress / 20;
        String amount = String.valueOf(seconds);
        String s = seconds < 2 ? "second" : "seconds";
        return ArrayUtils.toArray("machine.universal.consumption", "§7 ", "suffix", "machine.universal.liters.short",  "§r§7 (§b", this.fuelName, "§7)§r ", "every", "§b ", amount, "§r ", s);
    }

    @Override
    public String[] productionInfo() {
        int tier = GAUtility.getTierByVoltage(this.energyPerTick);
        String voltage = GAValues.VN[tier];
        String color = TJValues.VCC[tier];
        return ArrayUtils.toArray("machine.universal.producing", "§e ", "suffix", "§r ", "machine.universal.eu.tick",
                " ", "§7(§6", color, voltage, "§7)");
    }
}
