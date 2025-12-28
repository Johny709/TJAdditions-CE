package tj.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class TJCycleFuelRecipeLogic extends TJBoostableFuelRecipeLogic {

    private final Supplier<FluidStack> reagent;
    private int currentCycle;

    public TJCycleFuelRecipeLogic(MetaTileEntity metaTileEntity, FuelRecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer, Supplier<IMultipleTankHandler> fluidTank, Supplier<FluidStack> booster, Supplier<FluidStack> reagent, IntSupplier fuelMultiplier, IntSupplier euMultiplier, long maxVoltage) {
        super(metaTileEntity, recipeMap, energyContainer, fluidTank, booster, fuelMultiplier, euMultiplier, maxVoltage);
        this.reagent = reagent;
    }

    @Override
    protected boolean checkRecipe(FuelRecipe recipe) {
        if (this.currentCycle >= 19) {
            FluidStack reagent = this.fluidTank.get().drain(this.reagent.get(), true);
            if (reagent != null && reagent.isFluidStackIdentical(this.reagent.get()))
                this.currentCycle = 0;
            else {
                if (this.currentCycle < 20)
                    this.currentCycle--;
                return false;
            }
        } else this.currentCycle++;
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("cycles", this.currentCycle);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.currentCycle = compound.getInteger("cycles");
    }

    public int getCurrentCycle() {
        return this.currentCycle;
    }
}
