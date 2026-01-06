package tj.util;

import gregtech.api.capability.IMultipleTankHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class TJFluidUtils {

    public static long getFluidAmountFromTanks(FluidStack fluidStack, IMultipleTankHandler tanks) {
        long amount = 0;
        for (int i = 0; i < tanks.getTanks(); i++) {
            FluidStack stack = tanks.getTankAt(i).getFluid();
            if (stack != null && stack.isFluidEqual(fluidStack))
                amount += stack.amount;
        }
        return amount;
    }

    public static long getFluidCapacityFromTanks(FluidStack fluidStack, IMultipleTankHandler tanks) {
        long capacity = 0;
        for (int i = 0; i < tanks.getTanks(); i++) {
            IFluidTank tank = tanks.getTankAt(i);
            FluidStack stack = tank.getFluid();
            if (stack != null && stack.isFluidEqual(fluidStack))
                capacity += tank.getCapacity();
        }
        return capacity;
    }

    public static long getCapacityFromTanks(IMultipleTankHandler tanks) {
        long capacity = 0;
        for (int i = 0; i < tanks.getTanks(); i++)
            capacity += tanks.getTankAt(i).getCapacity();
        return capacity;
    }
}
