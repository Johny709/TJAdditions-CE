package tj.builder.handlers;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.ArrayUtils;
import tj.capability.impl.TJFuelRecipeLogic;
import tj.machines.multi.electric.MetaTileEntityLargeAtmosphereCollector;

import java.util.function.Supplier;

import static gregtech.api.unification.material.Materials.Air;
import static gregtech.api.unification.material.Materials.DistilledWater;

public class LargeAtmosphereCollectorWorkableHandler extends TJFuelRecipeLogic {

    private static final int CYCLE_LENGTH = 230;
    private static final int BASE_ROTOR_DAMAGE = 22;
    private static final long BASE_EU_OUTPUT = 512;

    private final MetaTileEntityLargeAtmosphereCollector airCollector;
    private int rotorCycleLength = CYCLE_LENGTH;

    private long totalAirProduced;
    private int fastModeMultiplier = 1;
    private int rotorDamageMultiplier = 1;
    private boolean isFastMode;
    private boolean fastMode;

    public LargeAtmosphereCollectorWorkableHandler(MetaTileEntityLargeAtmosphereCollector metaTileEntity, FuelRecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer, Supplier<IMultipleTankHandler> fluidTank) {
        super(metaTileEntity, recipeMap, energyContainer, fluidTank, 0L);
        this.airCollector = metaTileEntity;
    }

    @Override
    protected boolean startRecipe() {
        if (this.fastMode != this.isFastMode)
            this.toggleFastMode(this.fastMode);
        return ((this.airCollector.getProblems() >> 5) & 1) != 0 && this.isReadyForRecipes() && super.startRecipe();
    }

    @Override
    protected void progressRecipe(int progress) {
        this.exportFluidsSupplier.get().fill(Air.getFluid((int) this.totalAirProduced), true);
        this.progress++;
        if (this.airCollector.getOffsetTimer() % 20 == 0)
            this.totalAirProduced = this.getRecipeOutputVoltage();
    }

    @Override
    protected void stopRecipe() {
        super.stopRecipe();
        if (this.airCollector.getOffsetTimer() % 20 == 0)
            this.totalAirProduced = this.getRecipeOutputVoltage();
    }

    @Override
    protected void sleepRecipe() {
        super.sleepRecipe();
        if (this.airCollector.getOffsetTimer() % 20 == 0)
            this.totalAirProduced = this.getRecipeOutputVoltage();
    }

    @Override
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
        if (currentRecipe != null && this.checkRecipe(currentRecipe)) {
            int fuelAmountToUse = this.calculateFuelAmount(currentRecipe);
            if (fuelStack.amount >= fuelAmountToUse) {
                this.maxProgress = this.calculateRecipeDuration(currentRecipe);
                if (this.airCollector.turbineType == MetaTileEntityLargeTurbine.TurbineType.PLASMA)
                    this.exportFluidsSupplier.get().fill(FluidRegistry.getFluidStack(FluidRegistry.getFluidName(currentRecipe.getRecipeFluid()).substring(7), fuelAmountToUse), true);
                else if (this.airCollector.turbineType == MetaTileEntityLargeTurbine.TurbineType.STEAM)
                    this.exportFluidsSupplier.get().fill(DistilledWater.getFluid(fuelAmountToUse / 160), true);
                FluidStack recipeFluid = currentRecipe.getRecipeFluid();
                recipeFluid.amount = fuelAmountToUse;
                return recipeFluid;
            }
        }
        return null;
    }

    public void setFastMode(boolean isFastMode) {
        this.isFastMode = isFastMode;
        this.getMetaTileEntity().markDirty();
    }

    public boolean isFastMode() {
        return this.isFastMode;
    }

    private void toggleFastMode(boolean toggle) {
        if (toggle) {
            this.fastModeMultiplier = 3;
            this.rotorDamageMultiplier = 16;
        } else {
            this.fastModeMultiplier = 1;
            this.rotorDamageMultiplier = 1;
        }
    }

    @Override
    public boolean checkRecipe(FuelRecipe recipe) {
        MetaTileEntityRotorHolder rotorHolder = this.airCollector.getAbilities(MetaTileEntityLargeAtmosphereCollector.ABILITY_ROTOR_HOLDER).get(0);
        int baseRotorDamage = BASE_ROTOR_DAMAGE;
        if (++this.rotorCycleLength >= CYCLE_LENGTH) {
            if (this.airCollector.turbineType != MetaTileEntityLargeTurbine.TurbineType.STEAM) baseRotorDamage = 150;
            int damageToBeApplied = (int) Math.round(baseRotorDamage * rotorHolder.getRelativeRotorSpeed()) + 1;
            if (rotorHolder.applyDamageToRotor(damageToBeApplied, false)) {
                this.rotorCycleLength = 0;
                return true;
            } else return false;
        }
        return true;
    }

    @Override
    public long getMaxVoltage() {
        MetaTileEntityRotorHolder rotorHolder = this.airCollector.getAbilities(MetaTileEntityLargeAtmosphereCollector.ABILITY_ROTOR_HOLDER).get(0);
        if (rotorHolder.hasRotorInInventory()) {
            double rotorEfficiency = rotorHolder.getRotorEfficiency();
            double totalEnergyOutput = (BASE_EU_OUTPUT + getBonusForTurbineType(this.airCollector) * rotorEfficiency);
            return MathHelper.ceil(totalEnergyOutput * this.fastModeMultiplier);
        }
        return BASE_EU_OUTPUT + this.getBonusForTurbineType(this.airCollector);
    }

    @Override
    protected boolean isReadyForRecipes() {
        MetaTileEntityRotorHolder rotorHolder = this.airCollector.getAbilities(MetaTileEntityLargeAtmosphereCollector.ABILITY_ROTOR_HOLDER).get(0);
        return rotorHolder.isHasRotor();
    }

    private int getBonusForTurbineType(MetaTileEntityLargeAtmosphereCollector turbine) {
        switch (turbine.turbineType) {
            case GAS: return ConfigHolder.gasTurbineBonusOutput;
            case PLASMA: return ConfigHolder.plasmaTurbineBonusOutput;
            case STEAM: return ConfigHolder.steamTurbineBonusOutput;
            default: return 1;
        }
    }

    @Override
    public long getRecipeOutputVoltage() {
        double totalEnergyOutput;
        MetaTileEntityRotorHolder rotorHolder = this.airCollector.getAbilities(MetaTileEntityLargeAtmosphereCollector.ABILITY_ROTOR_HOLDER).get(0);
        double relativeRotorSpeed = rotorHolder.getRelativeRotorSpeed();
        if (rotorHolder.getCurrentRotorSpeed() > 0 && rotorHolder.hasRotorInInventory()) {
            double rotorEfficiency = rotorHolder.getRotorEfficiency();
            totalEnergyOutput = ((BASE_EU_OUTPUT + this.getBonusForTurbineType(this.airCollector)) * rotorEfficiency) * (relativeRotorSpeed * relativeRotorSpeed);
            totalEnergyOutput /= 1.00 + 0.05 * this.airCollector.getNumProblems();
            return MathHelper.ceil(totalEnergyOutput * this.fastModeMultiplier);
        }
        return 0L;
    }

    @Override
    protected int calculateFuelAmount(FuelRecipe currentRecipe) {
        int durationMultiplier = this.airCollector.turbineType == MetaTileEntityLargeTurbine.TurbineType.STEAM ? 2 : 1;
        return super.calculateFuelAmount(currentRecipe) * durationMultiplier;
    }

    @Override
    protected int calculateRecipeDuration(FuelRecipe currentRecipe) {
        int durationMultiplier = this.airCollector.turbineType == MetaTileEntityLargeTurbine.TurbineType.STEAM ? 2 : 1;
        return super.calculateRecipeDuration(currentRecipe) * durationMultiplier;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = super.serializeNBT();
        tagCompound.setInteger("cycleLength", this.rotorCycleLength);
        tagCompound.setInteger("fastModeMultiplier", this.fastModeMultiplier);
        tagCompound.setInteger("damageMultiplier", this.rotorDamageMultiplier);
        tagCompound.setBoolean("isFastMode", this.isFastMode);
        tagCompound.setLong("totalAir", this.totalAirProduced);
        tagCompound.setBoolean("fastMode", this.fastMode);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.rotorCycleLength = compound.getInteger("cycleLength");
        this.fastModeMultiplier = compound.getInteger("fastModeMultiplier");
        this.rotorDamageMultiplier = compound.getInteger("damageMultiplier");
        this.isFastMode = compound.getBoolean("isFastMode");
        this.fastMode = compound.getBoolean("fastMode");
        this.totalAirProduced = compound.getLong("totalAir");
    }

    @Override
    protected boolean shouldVoidExcessiveEnergy() {
        return true;
    }

    @Override
    public long getProduction() {
        return this.totalAirProduced;
    }

    @Override
    public String[] consumptionInfo() {
        int seconds = this.maxProgress / 20;
        String amount = String.valueOf(seconds);
        String s = seconds < 2 ? "second" : "seconds";
        return ArrayUtils.toArray("machine.universal.consumption", "§7 ", "suffix", "machine.universal.liters.short",  "§r§7(§b", this.fuelName, "§7)§r ", "every", "§b ", amount, "§r ", s);
    }

    @Override
    public String[] productionInfo() {
        return ArrayUtils.toArray("machine.universal.producing", "§b ", "suffix", "machine.universal.liters.short", "§r ", Air.getUnlocalizedName(), "machine.universal.tick");
    }
}
