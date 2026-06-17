package tj.capability.impl.workable;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import net.minecraft.util.math.MathHelper;
import tj.machines.multi.electric.MetaTileEntityLargeAtmosphereCollector;

import java.util.function.Supplier;


public class XLAtmosphereCollectorWorkableHandler extends LargeAtmosphereCollectorWorkableHandler {

    private static final float TURBINE_BONUS = 1.5f;
    private static final int BASE_EU_VOLTAGE = 512;

    public XLAtmosphereCollectorWorkableHandler(MetaTileEntityLargeAtmosphereCollector metaTileEntity, FuelRecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer, Supplier<IMultipleTankHandler> fluidTank) {
        super(metaTileEntity, recipeMap, energyContainer, fluidTank);
    }

    public static float getTurbineBonus() {
        float castTurbineBonus = 100 * TURBINE_BONUS;
        return (int) castTurbineBonus;
    }

    @Override
    public boolean checkRecipe(FuelRecipe recipe) {
        if (++this.rotorCycleLength >= CYCLE_LENGTH) {
            for (MetaTileEntityRotorHolder rotorHolder : this.airCollector.getAbilities(MetaTileEntityLargeAtmosphereCollector.ABILITY_ROTOR_HOLDER)) {
                int baseRotorDamage = BASE_ROTOR_DAMAGE;
                if (this.airCollector.turbineType != MetaTileEntityLargeTurbine.TurbineType.STEAM)
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
        int rotorHolderSize = this.airCollector.getAbilities(MetaTileEntityLargeAtmosphereCollector.ABILITY_ROTOR_HOLDER).size();
        for (int index = 0; index < rotorHolderSize; index++) {
            if (this.airCollector.getAbilities(MetaTileEntityLargeAtmosphereCollector.ABILITY_ROTOR_HOLDER).get(index).isHasRotor())
                areReadyForRecipes++;
        }
        return areReadyForRecipes == rotorHolderSize;
    }

    @Override
    public long getMaxVoltage() {
        double totalEnergyOutput = 0;
        for (MetaTileEntityRotorHolder rotorHolder : this.airCollector.getAbilities(MetaTileEntityLargeAtmosphereCollector.ABILITY_ROTOR_HOLDER)) {
            if (rotorHolder.hasRotorInInventory()) {
                totalEnergyOutput += BASE_EU_VOLTAGE + this.getBonusForTurbineType(this.airCollector) * rotorHolder.getRotorEfficiency();
            }
        }
        return MathHelper.ceil(totalEnergyOutput * this.fastModeMultiplier / 16);
    }

    @Override
    public long getRecipeOutputVoltage() {
        double totalEnergyOutput = 0;
        for (MetaTileEntityRotorHolder rotorHolder : this.airCollector.getAbilities(MetaTileEntityLargeAtmosphereCollector.ABILITY_ROTOR_HOLDER)) {
            if (rotorHolder.getCurrentRotorSpeed() > 0 && rotorHolder.hasRotorInInventory() && rotorHolder.isFrontFaceFree()) {
                final double relativeRotorSpeed = rotorHolder.getRelativeRotorSpeed();
                totalEnergyOutput += (BASE_EU_OUTPUT + this.getBonusForTurbineType(this.airCollector) * rotorHolder.getRotorEfficiency()) * (relativeRotorSpeed * relativeRotorSpeed);
            }
        }
        totalEnergyOutput /= 1.00 + 0.05 * this.airCollector.getNumProblems();
        return MathHelper.ceil(totalEnergyOutput * this.fastModeMultiplier * TURBINE_BONUS);
    }
}
