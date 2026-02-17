package tj.capability.impl.handler;

import net.minecraftforge.fluids.FluidStack;
import tj.capability.IMachineHandler;

public interface IMinerHandler extends IMachineHandler {

    int getFortuneLvl();

    FluidStack getDrillingFluid();
}
