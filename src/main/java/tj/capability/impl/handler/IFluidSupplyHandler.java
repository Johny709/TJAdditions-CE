package tj.capability.impl.handler;

import net.minecraftforge.fluids.FluidStack;

public interface IFluidSupplyHandler extends IRecipeHandler {

    FluidStack getFluidStack();
}
