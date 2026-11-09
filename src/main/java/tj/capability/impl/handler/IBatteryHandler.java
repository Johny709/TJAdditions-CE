package tj.capability.impl.handler;

import gregtech.api.capability.IEnergyContainer;
import gregtech.common.covers.CoverPump;
import tj.capability.IMachineHandler;

import java.util.List;

public interface IBatteryHandler extends IMachineHandler {

    CoverPump.PumpMode getPumpMode();

    BasicEnergyHandler getEnergyHandler();

    List<IEnergyContainer> getInputEnergy();

    List<IEnergyContainer> getOutputEnergy();
}
