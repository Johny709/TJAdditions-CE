package tj;


import tj.builder.SteamRecipeBuilder;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;

import static gregtech.api.unification.material.Materials.Steam;
import static gregtech.api.unification.material.Materials.Water;

public final class TJRecipeMaps {

    private TJRecipeMaps() {}

    public static final RecipeMap<SteamRecipeBuilder> PRIMITIVE_ALLOY_RECIPES = new RecipeMap<>("primitive_alloy.tj", 0, 2, 1, 1, 1, 1, 0, 0, new SteamRecipeBuilder().EUt(0).fluidInputs(Steam.getFluid(1000))).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SteamRecipeBuilder> COKE_OVEN_RECIPES = new RecipeMap<>("coke_oven_2.tj", 0, 1, 1, 1, 0, 0, 1, 1, new SteamRecipeBuilder().EUt(0)).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SteamRecipeBuilder> HEAT_EXCHANGER_RECIPES = new RecipeMap<>("heat_exchanger.tj", 0, 0, 0, 0, 1, 2, 1, 2, new SteamRecipeBuilder().EUt(0)).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> ARMOR_INFUSER_RECIPES = new RecipeMap<>("armor_infuser.tj", 0, 12, 0, 1, 0, 1, 0, 1, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> DRAGON_REPLICATOR_RECIPES = new RecipeMap<>("dragon_egg_replicator.tj", 0, 2, 0, 3, 0, 1, 0, 1, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> CHAOS_REPLICATOR_RECIPES = new RecipeMap<>("chaos_replicator.tj", 0, 4, 0, 2, 0, 1, 0, 0, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> LARGE_POWERED_SPAWNER_RECIPES = new RecipeMap<>("large_powered_spawner.tj", 0, 2, 0, 1, 0, 1, 0, 0, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> LARGE_VIAL_PROCESSOR_RECIPES = new RecipeMap<>("large_vial_processor.tj", 0, 1, 0, 14, 0, 0, 0, 1, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> GREENHOUSE_TREE_RECIPES = new RecipeMap<>("greenhouse_tree", 0, 2, 0, 8, 0, 1, 0, 0, new SimpleRecipeBuilder().EUt(7680).fluidInputs(Water.getFluid(2000))).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> ARCHITECT_RECIPES = new RecipeMap<>("architect", 1, 2, 0, 1, 0, 0, 0, 0, new SimpleRecipeBuilder().EUt(30)).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> ROCK_BREAKER_RECIPES = new RecipeMap<>("rock_breaker", 1, 1, 1, 1, 0, 2, 0, 0, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);
}
