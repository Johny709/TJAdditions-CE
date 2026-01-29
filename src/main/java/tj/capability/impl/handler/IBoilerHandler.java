package tj.capability.impl.handler;

import tj.capability.IMachineHandler;

public interface IBoilerHandler extends IMachineHandler {

    double getHeatEfficiencyMultiplier();

    double getFuelConsumptionMultiplier();

    int getBaseSteamOutput();

    int getMaxTemperature();
}
