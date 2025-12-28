package tj.capability;

import gregtech.api.recipes.RecipeMap;
import tj.machines.multi.BatchMode;

public interface IParallelController {

    default long getEnergyStored() {
        return 0L;
    }

    default long getEnergyCapacity() {
        return 0L;
    }

    default long getMaxEUt() {
        return 0;
    }

    default int getEUBonus() {
        return 0;
    }

    default long getTotalEnergyConsumption() {
        return 0;
    }

    default BatchMode getBatchMode() {
        return BatchMode.ONE;
    }

    long getVoltageTier();

    default RecipeMap<?> getMultiblockRecipe() {
        return null;
    }
}
