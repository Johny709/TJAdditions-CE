package tj.capability.impl.workable;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.ArrayUtils;
import tj.machines.multi.electric.MetaTileEntityXLTurbine;

import java.util.List;
import java.util.function.Supplier;

import static gregtech.api.unification.material.Materials.DistilledWater;
import static gregtech.common.metatileentities.multi.electric.generator.RotorHolderMultiblockController.ABILITY_ROTOR_HOLDER;

public class XLTurbineWorkableHandler extends TJFuelRecipeLogic {

    private static final float TURBINE_BONUS = 1.5f;
    private static final int CYCLE_LENGTH = 230;
    private static final int BASE_ROTOR_DAMAGE = 220;
    private static final int BASE_EU_OUTPUT = 2048;
    private static final int BASE_EU_VOLTAGE = 512;

    private final MetaTileEntityXLTurbine extremeTurbine;

    private boolean isFastMode;
    private boolean fastMode;
    private int fastModeMultiplier = 1;
    private int rotorDamageMultiplier = 1;

    private int rotorCycleLength = CYCLE_LENGTH;

    public XLTurbineWorkableHandler(MetaTileEntity metaTileEntity, FuelRecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer, Supplier<IMultipleTankHandler> importFluidTank, Supplier<IFluidHandler> exportFluidTank) {
        super(metaTileEntity, recipeMap, energyContainer, importFluidTank, 0L);
        this.exportFluidsSupplier = exportFluidTank;
        this.extremeTurbine = (MetaTileEntityXLTurbine) metaTileEntity;
        this.resetEnergy = false;
    }

    public static float getTurbineBonus() {
        float castTurbineBonus = 100 * TURBINE_BONUS;
        return (int) castTurbineBonus;
    }

    @Override
    protected boolean startRecipe() {
        if (this.fastMode != this.isFastMode)
            this.toggleFastMode(this.fastMode);
        return ((this.extremeTurbine.getProblems() >> 5) & 1) != 0 && this.isReadyForRecipes() && super.startRecipe();
    }

    @Override
    protected void progressRecipe(int progress) {
        super.progressRecipe(progress);
        if (this.extremeTurbine.getOffsetTimer() % 20 == 0)
            this.energyPerTick = this.getRecipeOutputVoltage();
    }

    @Override
    protected void stopRecipe() {
        super.stopRecipe();
        if (this.extremeTurbine.getOffsetTimer() % 20 == 0)
            this.energyPerTick = this.getRecipeOutputVoltage();
    }

    @Override
    protected void sleepRecipe() {
        super.sleepRecipe();
        if (this.extremeTurbine.getOffsetTimer() % 20 == 0)
            this.energyPerTick = this.getRecipeOutputVoltage();
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
                if (this.extremeTurbine.turbineType == MetaTileEntityLargeTurbine.TurbineType.PLASMA)
                    this.exportFluidsSupplier.get().fill(FluidRegistry.getFluidStack(FluidRegistry.getFluidName(currentRecipe.getRecipeFluid()).substring(7), fuelAmountToUse), true);
                else if (this.extremeTurbine.turbineType == MetaTileEntityLargeTurbine.TurbineType.STEAM)
                    this.exportFluidsSupplier.get().fill(DistilledWater.getFluid(fuelAmountToUse / 160), true);
                FluidStack recipeFluid = currentRecipe.getRecipeFluid();
                recipeFluid.amount = fuelAmountToUse;
                return recipeFluid;
            }
        }
        return null;
    }

    @Override
    public boolean checkRecipe(FuelRecipe recipe) {
        List<MetaTileEntityRotorHolder> rotorHolders = this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER);
        if (++this.rotorCycleLength >= CYCLE_LENGTH) {
            for (MetaTileEntityRotorHolder rotorHolder : rotorHolders) {
                int baseRotorDamage = BASE_ROTOR_DAMAGE;
                if (this.extremeTurbine.turbineType != MetaTileEntityLargeTurbine.TurbineType.STEAM)
                    baseRotorDamage = 150;
                int damageToBeApplied = (int) Math.round((baseRotorDamage * rotorHolder.getRelativeRotorSpeed()) + 1) * rotorDamageMultiplier;
                if (!rotorHolder.applyDamageToRotor(damageToBeApplied, false)) {
                    return false;
                }
            }
            this.rotorCycleLength = 0;
        }
        return true;
    }

    @Override
    protected boolean isReadyForRecipes() {
        int areReadyForRecipes = 0;
        int rotorHolderSize = this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER).size();
        for (int index = 0; index < rotorHolderSize; index++) {
            MetaTileEntityRotorHolder rotorHolder = this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER).get(index);
            if (rotorHolder.isHasRotor())
                areReadyForRecipes++;
        }
        return areReadyForRecipes == rotorHolderSize;
    }

    private int getBonusForTurbineType(MetaTileEntityXLTurbine turbine) {
        switch (turbine.turbineType) {
            case GAS: return ConfigHolder.gasTurbineBonusOutput;
            case PLASMA: return ConfigHolder.plasmaTurbineBonusOutput;
            case STEAM: return ConfigHolder.steamTurbineBonusOutput;
            default: return 1;
        }
    }

    @Override
    public long getMaxVoltage() {
        double totalEnergyOutput = 0;
        List<MetaTileEntityRotorHolder> rotorHolders = this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER);
        for (MetaTileEntityRotorHolder rotorHolder : rotorHolders) {
            if (rotorHolder.hasRotorInInventory()) {
                double rotorEfficiency = rotorHolder.getRotorEfficiency();
                totalEnergyOutput += BASE_EU_VOLTAGE + this.getBonusForTurbineType(this.extremeTurbine) * rotorEfficiency;
            }
        }
        return MathHelper.ceil(totalEnergyOutput * this.fastModeMultiplier / 16);
    }

    @Override
    public long getRecipeOutputVoltage() {
        double totalEnergyOutput = 0;
        for (MetaTileEntityRotorHolder rotorHolder : this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER)) {
            if (rotorHolder.getCurrentRotorSpeed() > 0 && rotorHolder.hasRotorInInventory() && rotorHolder.isFrontFaceFree()) {
                double relativeRotorSpeed = rotorHolder.getRelativeRotorSpeed();
                double rotorEfficiency = rotorHolder.getRotorEfficiency();
                totalEnergyOutput += (BASE_EU_OUTPUT + getBonusForTurbineType(this.extremeTurbine) * rotorEfficiency) * (relativeRotorSpeed * relativeRotorSpeed);
            }
        }
        totalEnergyOutput /= 1.00 + 0.05 * this.extremeTurbine.getNumProblems();
        return MathHelper.ceil(totalEnergyOutput * fastModeMultiplier * TURBINE_BONUS);
    }

    @Override
    protected int calculateFuelAmount(FuelRecipe currentRecipe) {
        int durationMultiplier = this.extremeTurbine.turbineType == MetaTileEntityLargeTurbine.TurbineType.STEAM ? 20 : 1;
        return (int) ((super.calculateFuelAmount(currentRecipe) * durationMultiplier) / (this.isFastMode ? 1 : TURBINE_BONUS));
    }

    @Override
    protected int calculateRecipeDuration(FuelRecipe currentRecipe) {
        int durationMultiplier = this.extremeTurbine.turbineType == MetaTileEntityLargeTurbine.TurbineType.STEAM ? 20 : 1;
        return super.calculateRecipeDuration(currentRecipe) * durationMultiplier;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = super.serializeNBT();
        tagCompound.setInteger("cycleLength", this.rotorCycleLength);
        tagCompound.setInteger("fastModeMultiplier", this.fastModeMultiplier);
        tagCompound.setInteger("damageMultiplier", this.rotorDamageMultiplier);
        tagCompound.setBoolean("isFastMode", this.isFastMode);
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
    }

    public void setFastMode(boolean fastMode) {
        this.fastMode = fastMode;
        this.getMetaTileEntity().markDirty();
    }

    public boolean isFastMode() {
        return this.fastMode;
    }

    private void toggleFastMode(boolean toggle) {
        for (MetaTileEntityRotorHolder rotorHolder : this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER))
            rotorHolder.resetRotorSpeed();
        this.isFastMode = toggle;
        if (toggle) {
            this.fastModeMultiplier = 3;
            this.rotorDamageMultiplier = 16;
        } else {
            this.fastModeMultiplier = 1;
            this.rotorDamageMultiplier = 1;
        }
    }

    @Override
    protected boolean shouldVoidExcessiveEnergy() {
        return true;
    }

    @Override
    public String[] consumptionInfo() {
        int seconds = this.maxProgress / 20;
        String amount = String.valueOf(seconds);
        String s = seconds < 2 ? "second" : "seconds";
        String color = this.extremeTurbine.turbineType == MetaTileEntityLargeTurbine.TurbineType.STEAM ? "§7 " : "§b ";
        return ArrayUtils.toArray("machine.universal.consumption", color, "suffix", "machine.universal.liters.short",  "§r§7(§b", this.fuelName, "§7)§r ", "every", "§b ", amount, "§r ", s);
    }
}

