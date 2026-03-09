package tj.capability;

import gregtech.api.recipes.CountableIngredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IGTRecipe {

    List<CountableIngredient> getMergedItemInputs();

    List<FluidStack> getMergedFluidInputs();

    void mergeRecipeInputs();
}
