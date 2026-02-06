package tj.capability.impl.workable;

import gregtech.api.capability.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import net.minecraftforge.fluids.FluidStack;
import tj.capability.IGeneratorInfo;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class TJBoostableFuelRecipeLogic extends TJFuelRecipeLogic implements IWorkable, IGeneratorInfo {

    private final IntSupplier euMultiplier;
    private final IntSupplier fuelMultiplier;
    private final Supplier<FluidStack> booster;
    private boolean boosted;

    public TJBoostableFuelRecipeLogic(MetaTileEntity metaTileEntity, FuelRecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer, Supplier<IMultipleTankHandler> fluidTank, Supplier<FluidStack> booster, IntSupplier fuelMultiplier, IntSupplier euMultiplier, long maxVoltage) {
        super(metaTileEntity, recipeMap, energyContainer, fluidTank, maxVoltage);
        this.euMultiplier = euMultiplier;
        this.fuelMultiplier = fuelMultiplier;
        this.booster = booster;
    }

    @Override
    protected int calculateFuelAmount(FuelRecipe currentRecipe) {
        FluidStack drainBooster = this.fluidTank.get().drain(this.booster.get(), false);
        this.boosted = drainBooster != null && drainBooster.amount >= this.booster.get().amount;
        return super.calculateFuelAmount(currentRecipe) * (this.boosted ? this.fuelMultiplier.getAsInt() : 1);
    }

    @Override
    protected long startRecipe(FuelRecipe currentRecipe, int fuelAmountUsed, int recipeDuration) {
        if (this.boosted)
            this.fluidTank.get().drain(this.booster.get(), true);
        return this.maxVoltage * (this.boosted ? this.euMultiplier.getAsInt() : 1);
    }

    public boolean isBoosted() {
        return boosted;
    }
}
