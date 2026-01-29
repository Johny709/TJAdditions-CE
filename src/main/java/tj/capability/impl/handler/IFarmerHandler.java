package tj.capability.impl.handler;

import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.IMachineHandler;

public interface IFarmerHandler extends IMachineHandler {

    IItemHandlerModifiable getToolInventory();

    IItemHandlerModifiable getFertilizerInventory();
}
