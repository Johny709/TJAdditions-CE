package tj.capability.impl.handler;

import gregtech.api.recipes.Recipe;
import tj.capability.IMachineHandler;
import tj.capability.OverclockManager;

public interface IRecipeHandler extends IMachineHandler {

    boolean checkRecipe(Recipe recipe);

    default void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {}

    default void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {}
}
