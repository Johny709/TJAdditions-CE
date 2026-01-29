package tj.capability;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.gui.widgets.impl.RecipeOutputDisplayWidget;

import java.util.List;
import java.util.function.DoubleSupplier;

public interface IRecipeMap {

    ModularUI.Builder createUITemplateAdvanced(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, RecipeOutputDisplayWidget displayWidget);

    void addInventorySlotGroupAdvanced(ModularUI.Builder builder, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isOutputs, RecipeOutputDisplayWidget displayWidget);

    Recipe findRecipeDistinct(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int outputFluidTankCapacity, boolean useOptimizedRecipeLookUp, List<Recipe> occupiedRecipes, boolean distinct);

    Recipe findByInputsAndOutputs(long voltage, List<ItemStack> inputs, List<ItemStack> outputs, List<FluidStack> fluidInputs, List<FluidStack> fluidOutputs);

    Recipe findRecipe(Recipe recipe);
}
