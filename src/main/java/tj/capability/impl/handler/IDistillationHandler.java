package tj.capability.impl.handler;

import gregtech.api.capability.IMultipleTankHandler;

public interface IDistillationHandler extends IRecipeHandler {

    IMultipleTankHandler getOutputHatchAt(int index);
}
