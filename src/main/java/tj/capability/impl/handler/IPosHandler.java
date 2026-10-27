package tj.capability.impl.handler;

import net.minecraft.util.math.BlockPos;
import tj.capability.IMachineHandler;

public interface IPosHandler extends IMachineHandler {

    BlockPos getBlockPos();
}
