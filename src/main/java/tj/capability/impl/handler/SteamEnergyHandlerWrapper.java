package tj.capability.impl.handler;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;

import static gregtech.api.unification.material.Materials.Steam;

public class SteamEnergyHandlerWrapper implements IEnergyContainer {

    private final IMultipleTankHandler fluidTanks;

    public SteamEnergyHandlerWrapper(IMultipleTankHandler fluidTank) {
        this.fluidTanks = fluidTank;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing enumFacing, long l, long l1) {
        return 0;
    }

    @Override
    public boolean inputsEnergy(EnumFacing enumFacing) {
        return false;
    }

    @Override
    public long changeEnergy(long energy) {
        if (energy == 0)
            return energy;
        if (energy < 0) {
            final FluidStack fluidStack = this.fluidTanks.drain((int) Math.abs(energy), true);
            return fluidStack == null ? 0 : -fluidStack.amount;
        } else return this.fluidTanks.fill(Steam.getFluid((int) energy), true);
    }

    @Override
    public long getEnergyStored() {
        long stored = 0;
        for (int i = 0; i < this.fluidTanks.getTanks(); i++) {
            stored += this.fluidTanks.getTankAt(i).getFluidAmount();
        }
        return stored;
    }

    @Override
    public long getEnergyCapacity() {
        long capacity = 0;
        for (int i = 0; i < this.fluidTanks.getTanks(); i++) {
            capacity += this.fluidTanks.getTankAt(i).getCapacity();
        }
        return capacity;
    }

    @Override
    public long getInputAmperage() {
        return 1;
    }

    @Override
    public long getInputVoltage() {
        return 32;
    }
}
