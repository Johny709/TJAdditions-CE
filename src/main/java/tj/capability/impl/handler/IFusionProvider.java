package tj.capability.impl.handler;

import gregtech.api.recipes.Recipe;
import tj.machines.multi.BatchMode;

public interface IFusionProvider {

    int getParallels();

    long getEnergyToStart();

    void setRecipe(long heat, Recipe recipe);

    default void replaceEnergyPortsAsActive(boolean active) {}

    default BatchMode getBatchMode() {
        return BatchMode.ONE;
    }
}
