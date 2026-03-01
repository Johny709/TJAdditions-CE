package tj.capability.impl.handler;

import net.minecraftforge.items.IItemHandlerModifiable;

public interface IAssemblyHandler extends IRecipeHandler {

    IItemHandlerModifiable getInputBusAt(int index);
}
