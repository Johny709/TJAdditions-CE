package tj.capability.impl.workable;

import gregtech.api.capability.IEnergyContainer;
import net.minecraft.util.EnumFacing;

public class EnderEnergyHandlerWrapper implements IEnergyContainer {

    private BasicEnergyHandler basicEnergyHandler;
    private long inputVoltage;
    private long outputVoltage;
    private long outputEnergyLimit = Integer.MIN_VALUE;
    private int inputAmps;
    private int outputAmps;

    public EnderEnergyHandlerWrapper(BasicEnergyHandler basicEnergyHandler) {
        this.basicEnergyHandler = basicEnergyHandler;
    }

    public EnderEnergyHandlerWrapper setBasicEnergyHandler(BasicEnergyHandler basicEnergyHandler) {
        this.basicEnergyHandler = basicEnergyHandler;
        this.reinitializeEnergyLimit();
        return this;
    }

    public EnderEnergyHandlerWrapper setInputVoltage(long inputVoltage) {
        this.inputVoltage = inputVoltage;
        return this;
    }

    public EnderEnergyHandlerWrapper setOutputVoltage(long outputVoltage) {
        this.outputVoltage = outputVoltage;
        return this;
    }

    public EnderEnergyHandlerWrapper setInputAmps(int inputAmps) {
        this.inputAmps = inputAmps;
        return this;
    }

    public EnderEnergyHandlerWrapper setOutputAmps(int outputAmps) {
        this.outputAmps = outputAmps;
        return this;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing enumFacing, long l, long l1) {
        return this.basicEnergyHandler.addEnergy(l);
    }

    @Override
    public boolean inputsEnergy(EnumFacing enumFacing) {
        return true;
    }

    @Override
    public long addEnergy(long energyToAdd) {
        return this.basicEnergyHandler.addEnergy(this.outputEnergyLimit != Integer.MIN_VALUE ? Math.min(energyToAdd, this.outputEnergyLimit) : energyToAdd);
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        return this.basicEnergyHandler.removeEnergy(energyToRemove);
    }

    @Override
    public long changeEnergy(long energy) {
        return this.basicEnergyHandler.changeEnergy(this.outputEnergyLimit != Integer.MIN_VALUE ? Math.min(energy, this.outputEnergyLimit) : energy);
    }

    @Override
    public long getEnergyStored() {
        return this.basicEnergyHandler.getEnergyStored();
    }

    @Override
    public long getEnergyCapacity() {
        return this.basicEnergyHandler.getEnergyCapacity();
    }

    @Override
    public long getOutputAmperage() {
        return this.outputAmps;
    }

    @Override
    public long getInputAmperage() {
        return this.inputAmps;
    }

    @Override
    public long getInputVoltage() {
        return this.inputVoltage;
    }

    @Override
    public long getOutputVoltage() {
        return this.outputVoltage;
    }

    public void reinitializeEnergyLimit() {
        if (this.outputVoltage > 0 && this.outputAmps > 0) {
            this.outputEnergyLimit = this.outputVoltage * this.outputAmps;
        } else this.outputEnergyLimit = Integer.MIN_VALUE;
    }
}
