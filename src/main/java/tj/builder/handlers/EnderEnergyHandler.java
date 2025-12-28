package tj.builder.handlers;

import gregtech.api.capability.IEnergyContainer;
import net.minecraft.util.EnumFacing;

public class EnderEnergyHandler implements IEnergyContainer {

    private BasicEnergyHandler basicEnergyHandler;
    private long inputVoltage;
    private long outputVoltage;
    private int inputAmps;
    private int outputAmps;

    public EnderEnergyHandler(BasicEnergyHandler basicEnergyHandler) {
        this.basicEnergyHandler = basicEnergyHandler;
    }

    public EnderEnergyHandler setBasicEnergyHandler(BasicEnergyHandler basicEnergyHandler) {
        this.basicEnergyHandler = basicEnergyHandler;
        return this;
    }

    public EnderEnergyHandler setInputVoltage(long inputVoltage) {
        this.inputVoltage = inputVoltage;
        return this;
    }

    public EnderEnergyHandler setOutputVoltage(long outputVoltage) {
        this.outputVoltage = outputVoltage;
        return this;
    }

    public EnderEnergyHandler setInputAmps(int inputAmps) {
        this.inputAmps = inputAmps;
        return this;
    }

    public EnderEnergyHandler setOutputAmps(int outputAmps) {
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
        return this.basicEnergyHandler.addEnergy(energyToAdd);
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        return this.basicEnergyHandler.removeEnergy(energyToRemove);
    }

    @Override
    public long changeEnergy(long l) {
        return this.basicEnergyHandler.changeEnergy(l);
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
}
