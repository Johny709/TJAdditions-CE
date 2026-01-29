package tj.capability;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJValues;

public interface IMachineHandler {

    default IItemHandlerModifiable getImportItemInventory() {
        return TJValues.DUMMY_ITEM_HANDLER;
    }

    default IItemHandlerModifiable getExportItemInventory() {
        return TJValues.DUMMY_ITEM_HANDLER;
    }

    default IItemHandlerModifiable getInputBus(int index) {
        return TJValues.DUMMY_ITEM_HANDLER;
    }

    default IMultipleTankHandler getImportFluidTank() {
        return TJValues.DUMMY_FLUID_HANDLER;
    }

    default IMultipleTankHandler getExportFluidTank() {
        return TJValues.DUMMY_FLUID_HANDLER;
    }

    default IEnergyContainer getInputEnergyContainer() {
        return TJValues.DUMMY_ENERGY;
    }

    default IEnergyContainer getOutputEnergyContainer() {
        return TJValues.DUMMY_ENERGY;
    }

    default int getTier() {
        return 0;
    }

    default int getParallel() {
        return 1;
    }

    default long getMaxVoltage() {
        return 0;
    }
}
