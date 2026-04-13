package tj.capability;

import tj.util.wrappers.GTFluidStackWrapper;
import tj.util.wrappers.GTIngredientWrapper;

import java.util.List;

public interface IGTRecipe {

    List<GTIngredientWrapper> getMergedItemInputs();

    List<GTFluidStackWrapper> getMergedFluidInputs();

    void mergeRecipeInputs();
}
