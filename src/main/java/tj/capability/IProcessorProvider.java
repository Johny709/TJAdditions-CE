package tj.capability;

import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.OrientedOverlayRenderer;

public interface IProcessorProvider {

    RecipeMap<?> getRecipeMap();

    OrientedOverlayRenderer getRendererOverlay();
}
