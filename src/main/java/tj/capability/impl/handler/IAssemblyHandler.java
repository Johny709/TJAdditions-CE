package tj.capability.impl.handler;

import gregicadditions.capabilities.IQubitContainer;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IAssemblyHandler extends IRecipeHandler {

    IItemHandlerModifiable getInputBusAt(int index);

    IQubitContainer getQubitContainer();
}
