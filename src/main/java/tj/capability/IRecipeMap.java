package tj.capability;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.gui.widgets.impl.RecipeOutputDisplayWidget;

import java.util.function.DoubleSupplier;

public interface IRecipeMap {

    ModularUI.Builder createUITemplateAdvanced(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, RecipeOutputDisplayWidget displayWidget);

    void addInventorySlotGroupAdvanced(ModularUI.Builder builder, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isOutputs, RecipeOutputDisplayWidget displayWidget);
}
