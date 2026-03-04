package tj.capability.impl.handler;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import tj.capability.IMachineHandler;
import tj.capability.OverclockManager;

public interface IMultiRecipeHandler extends IMachineHandler {

    RecipeMap<?> getRecipeMap();

    default boolean checkRecipe(Recipe recipe, int i) {
        return true;
    }

    default Recipe createRecipe(Recipe recipe, int i) {
        return recipe;
    }

    default void preOverclock(OverclockManager<?> overclockManager, Recipe recipe, int i) {}

    default void postOverclock(OverclockManager<?> overclockManager, Recipe recipe, int i) {}
}
