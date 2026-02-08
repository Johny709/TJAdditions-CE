package tj.capability.impl.handler;

import net.minecraft.util.math.BlockPos;
import tj.capability.IMachineHandler;

import java.util.Set;

public interface ICharcoalHandler extends IMachineHandler {

    boolean isAdvanced();

    Set<BlockPos> getCharcoalPos();

}
