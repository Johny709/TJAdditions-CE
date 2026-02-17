package tj.capability.impl.handler;

import net.minecraftforge.fluids.FluidStack;
import tj.capability.IMachineHandler;

public interface IMinerHandler extends IMachineHandler {

    /**
     * Miner doesn't apply the hammer effect if fortune lvl < 1.
     * @return fortune lvl
     */
    int getFortuneLvl();

    FluidStack getDrillingFluid();
}
