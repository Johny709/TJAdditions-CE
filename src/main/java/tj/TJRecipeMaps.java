package tj;


import gregicadditions.recipes.GARecipeMaps;
import gregicadditions.recipes.impl.LargeRecipeBuilder;
import gregicadditions.recipes.impl.LargeRecipeMap;
import gregtech.api.recipes.Recipe;
import tj.builder.recipes.RecipeMapLargeAssemblyLine;
import tj.builder.recipes.SteamRecipeBuilder;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;

import static gregtech.api.unification.material.Materials.Steam;
import static gregtech.api.unification.material.Materials.Water;

public final class TJRecipeMaps {

    private TJRecipeMaps() {}

    public static final RecipeMap<SteamRecipeBuilder> TJ_PRIMITIVE_ALLOY_RECIPES = new RecipeMap<>("primitive_alloy.tj", 0, 2, 1, 1, 1, 1, 0, 0, new SteamRecipeBuilder().EUt(0).fluidInputs(Steam.getFluid(1000))).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SteamRecipeBuilder> TJ_COKE_OVEN_RECIPES = new RecipeMap<>("coke_oven_2.tj", 0, 1, 1, 1, 0, 0, 1, 1, new SteamRecipeBuilder().EUt(0)).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SteamRecipeBuilder> TJ_HEAT_EXCHANGER_RECIPES = new RecipeMap<>("heat_exchanger.tj", 0, 0, 0, 0, 1, 2, 1, 2, new SteamRecipeBuilder().EUt(0)).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> TJ_ARMOR_INFUSER_RECIPES = new RecipeMap<>("armor_infuser.tj", 0, 12, 0, 1, 0, 1, 0, 1, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> TJ_DRAGON_REPLICATOR_RECIPES = new RecipeMap<>("dragon_egg_replicator.tj", 0, 2, 0, 3, 0, 1, 0, 1, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> TJ_CHAOS_REPLICATOR_RECIPES = new RecipeMap<>("chaos_replicator.tj", 0, 4, 0, 2, 0, 1, 0, 0, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> TJ_LARGE_POWERED_SPAWNER_RECIPES = new RecipeMap<>("large_powered_spawner.tj", 0, 2, 0, 1, 0, 1, 0, 0, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> TJ_LARGE_VIAL_PROCESSOR_RECIPES = new RecipeMap<>("large_vial_processor.tj", 0, 1, 0, 14, 0, 0, 0, 1, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> GREENHOUSE_TREE_RECIPES = new RecipeMap<>("greenhouse_tree", 0, 2, 0, 8, 0, 1, 0, 0, new SimpleRecipeBuilder().EUt(7680).fluidInputs(Water.getFluid(2000))).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> ARCHITECT_RECIPES = new RecipeMap<>("architect", 1, 2, 0, 1, 0, 0, 0, 0, new SimpleRecipeBuilder().EUt(30)).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> ROCK_BREAKER_RECIPES = new RecipeMap<>("rock_breaker", 1, 1, 1, 1, 0, 2, 0, 0, new SimpleRecipeBuilder()).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMapLargeAssemblyLine LARGE_ASSEMBLY_LINE_RECIPES = new RecipeMapLargeAssemblyLine("large_assembly_line", 1, 256, 1, 1, 0, 64, 0, 0);

    public static final LargeRecipeMap INTERSTELLAR_FORGE_RECIPES = (LargeRecipeMap) new LargeRecipeMap("interstellar_forge", 0, 6, 0, 6, 0, 6, 0, 6, (new LargeRecipeBuilder(GARecipeMaps.STELLAR_FORGE_RECIPES)))
            .setProgressBar(GuiTextures.PROGRESS_BAR_BATH, ProgressWidget.MoveType.HORIZONTAL);

    public static RecipeMap<?> COKE_OVEN_RECIPES;
    public static RecipeMap<?> PRIMITIVE_ALLOY_RECIPES;
    public static RecipeMap<?> HEAT_EXCHANGER_RECIPES;
    public static RecipeMap<?> ARMOR_INFUSER_RECIPES;
    public static RecipeMap<?> CHAOS_REPLICATOR_RECIPES;
    public static RecipeMap<?> DRAGON_REPLICATOR_RECIPES;
    public static RecipeMap<?> LARGE_POWERED_SPAWNER_RECIPES;
    public static RecipeMap<?> LARGE_VIAL_PROCESSOR_RECIPES;

    public static void registerLargeMachineRecipes(RecipeMap<?> mapToCopy, RecipeMap<LargeRecipeBuilder> mapToForm) {

        for (Recipe recipe : mapToCopy.getRecipeList()) {

            LargeRecipeBuilder largeRecipeBuilder = mapToForm.recipeBuilder()
                    .EUt(recipe.getEUt())
                    .duration(recipe.getDuration())
                    .inputsIngredients(recipe.getInputs())
                    .outputs(recipe.getOutputs())
                    .fluidInputs(recipe.getFluidInputs())
                    .fluidOutputs(recipe.getFluidOutputs());

            // TODO Giving better way to do this in GTCE
            for (Recipe.ChanceEntry entry : recipe.getChancedOutputs())
                largeRecipeBuilder.chancedOutput(entry.getItemStack(), entry.getChance(), entry.getBoostPerTier());

            largeRecipeBuilder.buildAndRegister();
        }
    }
}
