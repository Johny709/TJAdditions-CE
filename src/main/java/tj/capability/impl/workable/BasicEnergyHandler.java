package tj.capability.impl.workable;

import gregtech.api.capability.IEnergyContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class BasicEnergyHandler implements IEnergyContainer {

    private long stored;
    private long capacity;

    public BasicEnergyHandler(long capacity) {
        this.capacity = capacity;
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
    public long changeEnergy(long differenceAmount) {
        long oldEnergyStored = getEnergyStored();
        long newEnergyStored = (this.capacity - oldEnergyStored < differenceAmount) ? this.capacity : (oldEnergyStored + differenceAmount);
        if (newEnergyStored < 0)
            newEnergyStored = 0;
        this.stored = newEnergyStored;
        return newEnergyStored - oldEnergyStored;
    }

    @Override
    public long addEnergy(long energyToAdd) {
        return this.changeEnergy(energyToAdd);
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        return this.changeEnergy(-energyToRemove);
    }

    @Override
    public long getEnergyCapacity() {
        return this.capacity;
    }

    @Override
    public long getInputAmperage() {
        return 0;
    }

    @Override
    public long getInputVoltage() {
        return 0;
    }

    @Override
    public long getEnergyStored() {
        return this.stored;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("Stored", stored);
        nbt.setLong("Capacity", capacity);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        capacity = nbt.getLong("Capacity");
        stored = nbt.getLong("Stored");
    }
}
