package tj.capability.impl.handler;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import tj.capability.IMachineHandler;
import tj.capability.OverclockManager;

public interface IRecipeHandler extends IMachineHandler {

    RecipeMap<?> getRecipeMap();

    default boolean checkRecipe(Recipe recipe) {
        return true;
    }

    default Recipe recreateRecipe(Recipe recipe) {
        return recipe;
    }

    default void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {}

    default void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {}
}
