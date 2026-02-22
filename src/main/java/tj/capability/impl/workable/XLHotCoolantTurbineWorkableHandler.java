package tj.capability.impl.workable;


import gregicadditions.machines.multi.impl.MetaTileEntityRotorHolderForNuclearCoolant;
import gregicadditions.machines.multi.nuclear.MetaTileEntityHotCoolantTurbine;
import gregicadditions.recipes.impl.nuclear.HotCoolantRecipe;
import gregicadditions.recipes.impl.nuclear.HotCoolantRecipeMap;
import gregtech.api.capability.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.ConfigHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.ArrayUtils;
import tj.machines.multi.electric.MetaTileEntityXLHotCoolantTurbine;
import tj.mixin.gregicality.IMetaTileEntityRotorHolderForNuclearCoolantMixin;

import java.util.*;
import java.util.function.Supplier;

import static gregicadditions.machines.multi.nuclear.MetaTileEntityHotCoolantTurbine.ABILITY_ROTOR_HOLDER;


public class XLHotCoolantTurbineWorkableHandler extends TJGAFuelRecipeLogic {

    private static final float TURBINE_BONUS = 1.5f;
    private static final int CYCLE_LENGTH = 230;
    private static final int BASE_ROTOR_DAMAGE = 220;
    private static final int BASE_EU_OUTPUT = 2048;
    private static final int BASE_EU_VOLTAGE = 512;

    private final MetaTileEntityXLHotCoolantTurbine extremeTurbine;

    private boolean isFastMode;
    private boolean fastMode;
    private int fastModeMultiplier = 1;
    private int rotorDamageMultiplier = 1;

    private int rotorCycleLength = CYCLE_LENGTH;

    public XLHotCoolantTurbineWorkableHandler(MetaTileEntity metaTileEntity, HotCoolantRecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer, Supplier<IMultipleTankHandler> importFluidTank, Supplier<IFluidHandler> exportFluidTank) {
        super(metaTileEntity, recipeMap, energyContainer, importFluidTank, 0L);
        this.exportFluidsSupplier = exportFluidTank;
        this.extremeTurbine = (MetaTileEntityXLHotCoolantTurbine) metaTileEntity;
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
        HotCoolantRecipe currentRecipe;
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
                FluidStack outputFluid = currentRecipe.getOutputFluid().copy();
                outputFluid.amount = fuelAmountToUse;
                this.maxProgress = this.calculateRecipeDuration(currentRecipe);
                this.exportFluidsSupplier.get().fill(outputFluid, true);
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
                int damageToBeApplied = (int) Math.round((BASE_ROTOR_DAMAGE * rotorHolder.getRelativeRotorSpeed()) + 1) * rotorDamageMultiplier;
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
                totalEnergyOutput += BASE_EU_VOLTAGE + this.getBonusForTurbineType(this.extremeTurbine) * rotorEfficiency;
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
                totalEnergyOutput += (BASE_EU_OUTPUT + getBonusForTurbineType(this.extremeTurbine) * rotorEfficiency) * (relativeRotorSpeed * relativeRotorSpeed);
            }
        }
        totalEnergyOutput /= 1.00 + 0.05 * this.extremeTurbine.getNumProblems();
        return MathHelper.ceil(totalEnergyOutput * fastModeMultiplier * TURBINE_BONUS);
    }

    @Override
    protected int calculateFuelAmount(HotCoolantRecipe currentRecipe) {
        int durationMultiplier = 1;
        return (int) ((super.calculateFuelAmount(currentRecipe) * durationMultiplier) / (this.isFastMode ? 1 : TURBINE_BONUS));
    }

    @Override
    protected int calculateRecipeDuration(HotCoolantRecipe currentRecipe) {
        int durationMultiplier = 1;
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
        for (MetaTileEntityRotorHolderForNuclearCoolant rotorHolder : this.extremeTurbine.getAbilities(ABILITY_ROTOR_HOLDER))
            ((IMetaTileEntityRotorHolderForNuclearCoolantMixin) rotorHolder).setCurrentRotorSpeed(0);
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
        String color = "§b ";
        return ArrayUtils.toArray("machine.universal.consumption", color, "suffix", "machine.universal.liters.short",  "§r§7(§b", this.fuelName, "§7)§r ", "every", "§b ", amount, "§r ", s);
    }
}
