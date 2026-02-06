package tj.capability.impl.workable;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.machines.multi.impl.HotCoolantRecipeLogic;
import gregicadditions.machines.multi.impl.MetaTileEntityRotorHolderForNuclearCoolant;
import gregicadditions.machines.multi.nuclear.MetaTileEntityHotCoolantTurbine;
import gregicadditions.recipes.impl.nuclear.HotCoolantRecipe;
import gregicadditions.recipes.impl.nuclear.HotCoolantRecipeMap;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.FluidFuelInfo;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.ConfigHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJValues;
import tj.capability.IGeneratorInfo;
import tj.capability.TJCapabilities;
import tj.machines.multi.electric.MetaTileEntityXLHotCoolantTurbine;
import tj.mixin.gregicality.IMetaTileEntityRotorHolderForNuclearCoolantMixin;

import java.util.*;
import java.util.function.Supplier;

import static gregicadditions.machines.multi.nuclear.MetaTileEntityHotCoolantTurbine.ABILITY_ROTOR_HOLDER;

public class XLHotCoolantTurbineWorkableHandler extends HotCoolantRecipeLogic implements IWorkable, IGeneratorInfo, IFuelable {

    private static final float TURBINE_BONUS = 1.5f;
    private static final int CYCLE_LENGTH = 230;
    private static final int BASE_ROTOR_DAMAGE = 11;
    private static final int BASE_EU_OUTPUT = 2048;

    private final MetaTileEntityXLHotCoolantTurbine extremeTurbine;
    private final Supplier<IMultipleTankHandler> exportFluidTank;
    private final Set<FluidStack> lastSearchedFluid = new HashSet<>();
    private final Set<FluidStack> blacklistFluid = new HashSet<>();

    private String fuelName;
    private boolean isFastMode;
    private boolean fastMode;
    private boolean active;
    private int totalEnergyProduced;
    private int consumption;
    private int fastModeMultiplier = 1;
    private int rotorDamageMultiplier = 1;
    private int progress;
    private int maxProgress;
    private int searchCount;
    private int rotorCycleLength = CYCLE_LENGTH;

    public XLHotCoolantTurbineWorkableHandler(MetaTileEntity metaTileEntity, HotCoolantRecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer, Supplier<IMultipleTankHandler> importFluidTank, Supplier<IMultipleTankHandler> exportFluidTank) {
        super(metaTileEntity, recipeMap, energyContainer, importFluidTank, 0L);
        this.extremeTurbine = (MetaTileEntityXLHotCoolantTurbine) metaTileEntity;
        this.exportFluidTank = exportFluidTank;
    }

    public static float getTurbineBonus() {
        float castTurbineBonus = 100 * TURBINE_BONUS;
        return (int) castTurbineBonus;
    }

    @Override
    public void update() {
        if (this.getMetaTileEntity().getWorld().isRemote || !this.isWorkingEnabled())
            return;

        if (this.extremeTurbine.getOffsetTimer() % 20 == 0)
            this.totalEnergyProduced = (int) this.getRecipeOutputVoltage();

        if (this.totalEnergyProduced > 0)
            this.energyContainer.get().addEnergy(this.totalEnergyProduced);

        if (this.progress > 0 && !this.isActive())
            this.setActive(true);

        if (this.progress >= this.maxProgress) {
            this.extremeTurbine.calculateMaintenance(this.rotorDamageMultiplier * this.maxProgress);
            this.lastSearchedFluid.clear();
            this.progress = 0;
            this.setActive(false);
        }

        if (this.progress <= 0) {
            if (this.fastMode != this.isFastMode)
                this.toggleFastMode(this.isFastMode);
            if (this.extremeTurbine.getNumProblems() >= 6 || !this.isReadyForRecipes() || !this.tryAcquireNewRecipe())
                return;
            this.progress = 1;
            this.setActive(true);
        } else {
            this.progress++;
        }
    }

    public void setFastMode(boolean fastMode) {
        this.fastMode = fastMode;
        this.getMetaTileEntity().markDirty();
    }

    public boolean isFastMode() {
        return this.fastMode;
    }

    private void toggleFastMode(boolean toggle) {
        for (MetaTileEntityRotorHolderForNuclearCoolant rotorHolder : this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER)) {
            ((IMetaTileEntityRotorHolderForNuclearCoolantMixin) rotorHolder).setCurrentRotorSpeed(0);
            rotorHolder.markDirty();
        }
        this.isFastMode = toggle;
        if (toggle) {
            this.fastModeMultiplier = 3;
            this.rotorDamageMultiplier = 16;
        } else {
            this.fastModeMultiplier = 1;
            this.rotorDamageMultiplier = 1;
        }
    }

    public FluidStack getFuelStack() {
        if (this.previousRecipe == null)
            return null;
        FluidStack fuelStack = this.previousRecipe.getRecipeFluid();
        return this.fluidTank.get().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false);
    }

    protected boolean tryAcquireNewRecipe() {
        FluidStack fuelStack = null;
        for (int i = 0; i < this.fluidTank.get().getTanks(); i++) {
            IFluidTank tank = this.fluidTank.get().getTankAt(i);
            FluidStack stack = tank.getFluid();
            if (stack == null) continue;
            if (fuelStack == null) {
                if (this.lastSearchedFluid.contains(stack)) continue;
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
            return true; //recipe is found and ready to use
        }
        if (++this.searchCount >= this.fluidTank.get().getTanks()) {
            this.lastSearchedFluid.clear();
            this.searchCount = 0;
        }
        return false;
    }

    private FluidStack tryAcquireNewRecipe(FluidStack fuelStack) {
        HotCoolantRecipe currentRecipe;
        if (this.previousRecipe != null && this.previousRecipe.matches(this.getMaxVoltage(), fuelStack)) {
            //if previous recipe still matches inputs, try to use it
            currentRecipe = this.previousRecipe;
        } else {
            //else, try searching new recipe for given inputs
            currentRecipe = recipeMap.findRecipe(this.getMaxVoltage(), fuelStack);
            //if we found recipe that can be buffered, buffer it
            if (currentRecipe != null) {
                this.previousRecipe = currentRecipe;
            } else this.blacklistFluid.add(fuelStack); // blacklist fluid not found in recipe map to prevent search slowdown.
        }
        if (currentRecipe != null && this.checkRecipe(currentRecipe)) {
            int fuelAmountToUse = this.calculateFuelAmount(currentRecipe);
            if (fuelStack.amount >= fuelAmountToUse) {
                this.maxProgress = this.calculateRecipeDuration(currentRecipe);
                FluidStack outputFluid = currentRecipe.getOutputFluid();
                outputFluid.amount = fuelAmountToUse;
                this.exportFluidTank.get().fill(outputFluid, true);
                FluidStack recipeFluid = currentRecipe.getRecipeFluid();
                recipeFluid.amount = fuelAmountToUse;
                return recipeFluid;
            }
        }
        return null;
    }

    @Override
    public boolean checkRecipe(HotCoolantRecipe recipe) {
        List<MetaTileEntityRotorHolderForNuclearCoolant> rotorHolders = this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER);
        if (++this.rotorCycleLength >= CYCLE_LENGTH) {
            for (MetaTileEntityRotorHolderForNuclearCoolant rotorHolder : rotorHolders) {
                int damageToBeApplied = (int) Math.round((BASE_ROTOR_DAMAGE * rotorHolder.getRelativeRotorSpeed()) + 1) * this.rotorDamageMultiplier;
                if (!rotorHolder.applyDamageToRotor(damageToBeApplied, false)) {
                    return false;
                }
            }
            this.rotorCycleLength = 0;
        }
        return true;
    }

    protected boolean isReadyForRecipes() {
        int areReadyForRecipes = 0;
        int rotorHolderSize = this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER).size();
        for (int index = 0; index < rotorHolderSize; index++) {
            MetaTileEntityRotorHolderForNuclearCoolant rotorHolder = this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER).get(index);
            if (rotorHolder.isHasRotor())
                areReadyForRecipes++;
        }
        return areReadyForRecipes == rotorHolderSize;
    }

    private int getBonusForTurbineType(MetaTileEntityXLHotCoolantTurbine turbine) {
        if (turbine.turbineType == MetaTileEntityHotCoolantTurbine.TurbineType.HOT_COOLANT) {
            return ConfigHolder.steamTurbineBonusOutput * 130 / 100;
        }
        return 1;
    }

    @Override
    public long getMaxVoltage() {
        double totalEnergyOutput = 0;
        List<MetaTileEntityRotorHolderForNuclearCoolant> rotorHolders = this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER);
        for (MetaTileEntityRotorHolderForNuclearCoolant rotorHolder : rotorHolders) {
            if (rotorHolder.hasRotorInInventory()) {
                double rotorEfficiency = rotorHolder.getRotorEfficiency();
                totalEnergyOutput += BASE_EU_OUTPUT + this.getBonusForTurbineType(this.extremeTurbine) * rotorEfficiency;
            }
        }
        return MathHelper.ceil(totalEnergyOutput * this.fastModeMultiplier / 16);
    }

    @Override
    public long getRecipeOutputVoltage() {
        double totalEnergyOutput = 0;
        for (MetaTileEntityRotorHolderForNuclearCoolant rotorHolder : this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER)) {
            if (rotorHolder.getCurrentRotorSpeed() > 0 && rotorHolder.hasRotorInInventory() && rotorHolder.isFrontFaceFree()) {
                double relativeRotorSpeed = rotorHolder.getRelativeRotorSpeed();
                double rotorEfficiency = rotorHolder.getRotorEfficiency();
                totalEnergyOutput += (BASE_EU_OUTPUT + this.getBonusForTurbineType(this.extremeTurbine) * rotorEfficiency) * (relativeRotorSpeed * relativeRotorSpeed);
            }
        }
        totalEnergyOutput /= 1.00 + 0.05 * this.extremeTurbine.getNumProblems();
        return MathHelper.ceil(totalEnergyOutput * this.fastModeMultiplier * TURBINE_BONUS);
    }

    @Override
    protected int calculateFuelAmount(HotCoolantRecipe currentRecipe) {
        return (int) (super.calculateFuelAmount(currentRecipe) / (this.isFastMode ? 1 : TURBINE_BONUS));
    }

    @Override
    public void writeInitialData(PacketBuffer buf) {
        buf.writeBoolean(this.active);
    }

    @Override
    public void receiveInitialData(PacketBuffer buf) {
        this.active = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 10) {
            this.active = buf.readBoolean();
            this.extremeTurbine.scheduleRenderUpdate();
        }
    }

    private void setActive(boolean active) {
        this.active = active;
        if (!this.getMetaTileEntity().getWorld().isRemote) {
            this.writeCustomData(10, buf -> buf.writeBoolean(active));
            this.getMetaTileEntity().markDirty();
        }
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = super.serializeNBT();
        tagCompound.setInteger("CycleLength", this.rotorCycleLength);
        tagCompound.setInteger("FastModeMultiplier", this.fastModeMultiplier);
        tagCompound.setInteger("DamageMultiplier", this.rotorDamageMultiplier);
        tagCompound.setBoolean("IsFastMode", this.isFastMode);
        tagCompound.setBoolean("FastMode", this.fastMode);
        tagCompound.setInteger("Consumption", this.consumption);
        tagCompound.setInteger("TotalEnergy", this.totalEnergyProduced);
        tagCompound.setInteger("Progress", this.progress);
        tagCompound.setInteger("MaxProgress", this.maxProgress);
        tagCompound.setBoolean("Active", this.active);
        if (this.fuelName != null)
            tagCompound.setString("FuelName", this.fuelName);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.rotorCycleLength = compound.getInteger("CycleLength");
        this.fastModeMultiplier = compound.getInteger("FastModeMultiplier");
        this.rotorDamageMultiplier = compound.getInteger("DamageMultiplier");
        this.isFastMode = compound.getBoolean("IsFastMode");
        this.fastMode = compound.getBoolean("FastMode");
        this.consumption = compound.getInteger("Consumption");
        this.totalEnergyProduced = compound.getInteger("TotalEnergy");
        this.maxProgress = compound.getInteger("MaxProgress");
        this.progress = compound.getInteger("Progress");
        this.active = compound.getBoolean("Active");
        if (compound.hasKey("FuelName"))
            this.fuelName = compound.getString("FuelName");
    }

    @Override
    protected boolean shouldVoidExcessiveEnergy() {
        return true;
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        if (capability == TJCapabilities.CAPABILITY_GENERATOR)
            return TJCapabilities.CAPABILITY_GENERATOR.cast(this);
        if (capability == GregtechCapabilities.CAPABILITY_FUELABLE)
            return GregtechCapabilities.CAPABILITY_FUELABLE.cast(this);
        return super.getCapability(capability);
    }

    public String getFuelName() {
        return this.fuelName;
    }

    @Override
    public int getProgress() {
        return this.progress;
    }

    @Override
    public int getMaxProgress() {
        return this.maxProgress;
    }

    @Override
    public long getConsumption() {
        return this.consumption;
    }

    @Override
    public long getProduction() {
        return this.totalEnergyProduced;
    }

    @Override
    public String[] consumptionInfo() {
        int seconds = this.maxProgress / 20;
        String amount = String.valueOf(seconds);
        String s = seconds < 2 ? "second" : "seconds";
        return ArrayUtils.toArray("machine.universal.consumption", "§b ", "suffix", "machine.universal.liters.short",  "§r§7(§b", this.fuelName, "§7)§r ", "every", "§b ", amount, "§r ", s);
    }

    @Override
    public String[] productionInfo() {
        int tier = GAUtility.getTierByVoltage(this.totalEnergyProduced);
        String voltage = GAValues.VN[tier];
        String color = TJValues.VCC[tier];
        return ArrayUtils.toArray("machine.universal.producing", "§e ", "suffix", "§r ", "machine.universal.eu.tick",
                " ", "§r§7(§6", color, voltage, "§7)");
    }

    // Similar to tryAcquire but with no side effects
    private HotCoolantRecipe findRecipe(FluidStack fluidStack) {
        HotCoolantRecipe currentRecipe;
        if (previousRecipe != null && previousRecipe.matches(getMaxVoltage(), fluidStack)) {
            currentRecipe = previousRecipe;
        } else {
            currentRecipe = recipeMap.findRecipe(getMaxVoltage(), fluidStack);
        }
        if (currentRecipe != null && checkRecipe(currentRecipe))
            return currentRecipe;
        return null;
    }

    @Override
    public Collection<IFuelInfo> getFuels() {
        if (!isReadyForRecipes())
            return Collections.emptySet();
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
            HotCoolantRecipe recipe = findRecipe(tankContents);
            if (recipe == null || this.lastSearchedFluid.contains(recipe.getRecipeFluid())) {
                fuelCapacity -= fluidTank.getCapacity();
                continue;
            }
            int amountPerRecipe = calculateFuelAmount(recipe);
            int duration = calculateRecipeDuration(recipe);
            long fuelBurnTime = ((long) duration * fuelRemaining) / amountPerRecipe;

            FluidFuelInfo fuelInfo = (FluidFuelInfo) fuels.get(tankContents.getUnlocalizedName());
            if (fuelInfo == null) {
                fuelInfo = new FluidFuelInfo(tankContents, fuelRemaining, fuelCapacity, amountPerRecipe, fuelBurnTime);
                fuels.put(tankContents.getUnlocalizedName(), fuelInfo);
            }
            else {
                fuelInfo.addFuelRemaining(fuelRemaining);
                fuelInfo.addFuelBurnTime(fuelBurnTime);
            }
        }
        return fuels.values();
    }
}
