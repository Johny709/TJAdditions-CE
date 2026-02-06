package tj.util;

import gregtech.api.capability.IMultipleTankHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import java.util.function.BiConsumer;

public final class TJFluidUtils {

    private TJFluidUtils() {}

    public static long getFluidAmountFromTanks(FluidStack fluidStack, IMultipleTankHandler tanks) {
        if (fluidStack == null || tanks == null)
            return 0;
        long amount = 0;
        for (int i = 0; i < tanks.getTanks(); i++) {
            FluidStack stack = tanks.getTankAt(i).getFluid();
            if (stack != null && stack.isFluidEqual(fluidStack))
                amount += stack.amount;
        }
        return amount;
    }

    public static long getFluidCapacityFromTanks(FluidStack fluidStack, IMultipleTankHandler tanks) {
        if (fluidStack == null || tanks == null)
            return 0;
        long capacity = 0;
        for (int i = 0; i < tanks.getTanks(); i++) {
            IFluidTank tank = tanks.getTankAt(i);
            FluidStack stack = tank.getFluid();
            if (stack == null || stack.isFluidEqual(fluidStack))
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

    /**
     * Tries to insert into fluid tanks or fluid handler.
     * @param tanks fluid container inventory
     * @param fluidStack the FluidStack to insert
     * @param doFill test to see if the item can be inserted without actually inserting the item for real.
     * @return FluidStack reminder. returns with 0 amount when FluidStack is fully inserted. returns the stack unmodified when unable to insert at all.
     */
    public static FluidStack fillIntoTanks(IMultipleTankHandler tanks, FluidStack fluidStack, boolean doFill) {
        if (fluidStack == null || tanks == null)
            return fluidStack;
        fluidStack = doFill ? fluidStack.copy() : fluidStack;
        for (int i = 0; i < tanks.getTanks() && fluidStack.amount > 0; i++) {
            IFluidTank tank = tanks.getTankAt(i);
            FluidStack slotStack = tank.getFluid();
            if (slotStack == null) {
                fluidStack.amount -= tank.fill(fluidStack, doFill);
            } else if (slotStack.isFluidEqual(fluidStack)) {
                int reminder = Math.max(0, tank.getCapacity() - slotStack.amount);
                int inserted = Math.min(fluidStack.amount, reminder);
                fluidStack.amount -= inserted;
                if (doFill) {
                    slotStack.amount += inserted;
                }
            }
        }
        return fluidStack;
    }

    /**
     * Tries to insert into fluid tanks or fluid handler. Only recommended for client-side simulations.
     * @param tanks fluid container inventory
     * @param fluidStack the FluidStack to insert
     * @param doFill test to see if the item can be inserted without actually inserting the fluid for real.
     * @param beforeInsertedCallback runs callback before the fluid gets inserted.
     * @param afterInsertedCallback runs callback after the fluid has been inserted.
     * @return FluidStack reminder. returns with 0 amount when FluidStack is fully inserted. returns the stack unmodified when unable to insert at all.
     */
    public static FluidStack fillIntoTanksWithCallback(IMultipleTankHandler tanks, FluidStack fluidStack, boolean doFill, BiConsumer<Integer, FluidStack> beforeInsertedCallback, BiConsumer<Integer, FluidStack> afterInsertedCallback) {
        if (fluidStack == null || tanks == null)
            return fluidStack;
        fluidStack = doFill ? fluidStack : fluidStack.copy();
        for (int i = 0; i < tanks.getTanks() && fluidStack.amount > 0; i++) {
            IFluidTank tank = tanks.getTankAt(i);
            FluidStack slotStack = tank.getFluid();
            if (slotStack == null) {
                beforeInsertedCallback.accept(i, fluidStack);
                fluidStack.amount -= tank.fill(fluidStack, doFill);
                afterInsertedCallback.accept(i, fluidStack);
            } else if (slotStack.isFluidEqual(fluidStack)) {
                beforeInsertedCallback.accept(i, fluidStack);
                int reminder = Math.max(0, tank.getCapacity() - slotStack.amount);
                int inserted = Math.min(fluidStack.amount, reminder);
                fluidStack.amount -= inserted;
                if (doFill) {
                    slotStack.amount += inserted;
                }
                afterInsertedCallback.accept(i, fluidStack);
            }
        }
        return fluidStack;
    }
}
