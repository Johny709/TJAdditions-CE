package tj;


import tj.builder.ParallelRecipeMap;
import tj.builder.SteamRecipeBuilder;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;

import static gregicadditions.recipes.GARecipeMaps.*;
import static gregtech.api.recipes.RecipeMaps.*;
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

    public static ParallelRecipeMap PARALLEL_CHEMICAL_REACTOR_RECIPES;
    public static ParallelRecipeMap PARALLEL_CHEMICAL_PLANT_RECIPES;
    public static ParallelRecipeMap PARALLEL_MACERATOR_RECIPES;
    public static ParallelRecipeMap PARALLEL_ORE_WASHER_RECIPES;
    public static ParallelRecipeMap PARALLEL_CHEMICAL_BATH_RECIPES;
    public static ParallelRecipeMap PARALLEL_SIMPLE_ORE_WASHER_RECIPES;
    public static ParallelRecipeMap PARALLEL_AUTOCLAVE_RECIPES;
    public static ParallelRecipeMap PARALLEL_CENTRIFUGE_RECIPES;
    public static ParallelRecipeMap PARALLEL_THERMAL_CENTRIFUGE_RECIPES;
    public static ParallelRecipeMap PARALLEL_GAS_CENTRIFUGE_RECIPES;
    public static ParallelRecipeMap PARALLEL_SIFTER_RECIPES;
    public static ParallelRecipeMap PARALLEL_ELECTROLYZER_RECIPES;
    public static ParallelRecipeMap PARALLEL_BREWING_MACHINE_RECIPES;
    public static ParallelRecipeMap PARALLEL_FERMENTING_RECIPES;
    public static ParallelRecipeMap PARALLEL_CHEMICAL_DEHYDRATOR_RECIPES;
    public static ParallelRecipeMap PARALLEL_CRACKING_UNIT_RECIPES;
    public static ParallelRecipeMap PARALLEL_ARC_FURNACE_RECIPES;
    public static ParallelRecipeMap PARALLEL_PLASMA_ARC_FURNACE_RECIPES;
    public static ParallelRecipeMap PARALLEL_ASSEMBLER_RECIPES;
    public static ParallelRecipeMap PARALLEL_BENDER_RECIPES;
    public static ParallelRecipeMap PARALLEL_FORMING_PRESS_RECIPES;
    public static ParallelRecipeMap PARALLEL_CLUSTER_MILL_RECIPES;
    public static ParallelRecipeMap PARALLEL_CANNER_RECIPES;
    public static ParallelRecipeMap PARALLEL_FLUID_CANNER_RECIPES;
    public static ParallelRecipeMap PARALLEL_FLUID_SOLIDIFICATION_RECIPES;
    public static ParallelRecipeMap PARALLEL_CUTTER_RECIPES;
    public static ParallelRecipeMap PARALLEL_LATHE_RECIPES;
    public static ParallelRecipeMap PARALLEL_POLARIZER_RECIPES;
    public static ParallelRecipeMap PARALLEL_ELECTROMAGNETIC_SEPARATOR_RECIPES;
    public static ParallelRecipeMap PARALLEL_FLUID_EXTRACTION_RECIPES;
    public static ParallelRecipeMap PARALLEL_EXTRACTOR_RECIPES;
    public static ParallelRecipeMap PARALLEL_EXTRUDER_RECIPES;
    public static ParallelRecipeMap PARALLEL_FORGE_HAMMER_RECIPES;
    public static ParallelRecipeMap PARALLEL_COMPRESSOR_RECIPES;
    public static ParallelRecipeMap PARALLEL_LARGE_ENGRAVER_RECIPES;
    public static ParallelRecipeMap PARALLEL_LARGE_MIXER_RECIPES;
    public static ParallelRecipeMap PARALLEL_PACKER_RECIPES;
    public static ParallelRecipeMap PARALLEL_UNPACKER_RECIPES;
    public static ParallelRecipeMap PARALLEL_WIREMILL_RECIPES;
    public static ParallelRecipeMap PARALLEL_PLASMA_CONDENSER_RECIPES;
    public static ParallelRecipeMap PARALLEL_BLAST_ALLOY_RECIPES;
    public static ParallelRecipeMap PARALLEL_BLAST_RECIPES;
    public static ParallelRecipeMap PARALLEL_VACUUM_RECIPES;

    public static void parallelRecipesInit() {
        PARALLEL_CHEMICAL_REACTOR_RECIPES = new ParallelRecipeMap(LARGE_CHEMICAL_RECIPES);
        PARALLEL_CHEMICAL_PLANT_RECIPES = new ParallelRecipeMap(CHEMICAL_PLANT_RECIPES);
        PARALLEL_MACERATOR_RECIPES = new ParallelRecipeMap(MACERATOR_RECIPES);
        PARALLEL_ORE_WASHER_RECIPES = new ParallelRecipeMap(ORE_WASHER_RECIPES);
        PARALLEL_CHEMICAL_BATH_RECIPES = new ParallelRecipeMap(CHEMICAL_BATH_RECIPES);
        PARALLEL_SIMPLE_ORE_WASHER_RECIPES = new ParallelRecipeMap(SIMPLE_ORE_WASHER_RECIPES);
        PARALLEL_AUTOCLAVE_RECIPES = new ParallelRecipeMap(AUTOCLAVE_RECIPES);
        PARALLEL_CENTRIFUGE_RECIPES = new ParallelRecipeMap(LARGE_CENTRIFUGE_RECIPES);
        PARALLEL_THERMAL_CENTRIFUGE_RECIPES = new ParallelRecipeMap(THERMAL_CENTRIFUGE_RECIPES);
        PARALLEL_GAS_CENTRIFUGE_RECIPES = new ParallelRecipeMap(GAS_CENTRIFUGE_RECIPES);
        PARALLEL_SIFTER_RECIPES = new ParallelRecipeMap(SIFTER_RECIPES);
        PARALLEL_ELECTROLYZER_RECIPES = new ParallelRecipeMap(ELECTROLYZER_RECIPES);
        PARALLEL_BREWING_MACHINE_RECIPES = new ParallelRecipeMap(BREWING_RECIPES);
        PARALLEL_FERMENTING_RECIPES = new ParallelRecipeMap(FERMENTING_RECIPES);
        PARALLEL_CHEMICAL_DEHYDRATOR_RECIPES = new ParallelRecipeMap(CHEMICAL_DEHYDRATOR_RECIPES);
        PARALLEL_CRACKING_UNIT_RECIPES = new ParallelRecipeMap(CRACKING_RECIPES);
        PARALLEL_ARC_FURNACE_RECIPES = new ParallelRecipeMap(ARC_FURNACE_RECIPES);
        PARALLEL_PLASMA_ARC_FURNACE_RECIPES = new ParallelRecipeMap(PLASMA_ARC_FURNACE_RECIPES);
        PARALLEL_ASSEMBLER_RECIPES = new ParallelRecipeMap(ASSEMBLER_RECIPES);
        PARALLEL_BENDER_RECIPES = new ParallelRecipeMap(BENDER_RECIPES);
        PARALLEL_FORMING_PRESS_RECIPES = new ParallelRecipeMap(FORMING_PRESS_RECIPES);
        PARALLEL_CLUSTER_MILL_RECIPES = new ParallelRecipeMap(CLUSTER_MILL_RECIPES);
        PARALLEL_CANNER_RECIPES = new ParallelRecipeMap(CANNER_RECIPES);
        PARALLEL_FLUID_CANNER_RECIPES = new ParallelRecipeMap(FLUID_CANNER_RECIPES);
        PARALLEL_FLUID_SOLIDIFICATION_RECIPES = new ParallelRecipeMap(FLUID_SOLIDFICATION_RECIPES);
        PARALLEL_CUTTER_RECIPES = new ParallelRecipeMap(CUTTER_RECIPES);
        PARALLEL_LATHE_RECIPES = new ParallelRecipeMap(LATHE_RECIPES);
        PARALLEL_POLARIZER_RECIPES = new ParallelRecipeMap(POLARIZER_RECIPES);
        PARALLEL_ELECTROMAGNETIC_SEPARATOR_RECIPES = new ParallelRecipeMap(ELECTROMAGNETIC_SEPARATOR_RECIPES);
        PARALLEL_FLUID_EXTRACTION_RECIPES = new ParallelRecipeMap(FLUID_EXTRACTION_RECIPES);
        PARALLEL_EXTRACTOR_RECIPES = new ParallelRecipeMap(EXTRACTOR_RECIPES);
        PARALLEL_EXTRUDER_RECIPES = new ParallelRecipeMap(EXTRUDER_RECIPES);
        PARALLEL_FORGE_HAMMER_RECIPES = new ParallelRecipeMap(FORGE_HAMMER_RECIPES);
        PARALLEL_COMPRESSOR_RECIPES = new ParallelRecipeMap(COMPRESSOR_RECIPES);
        PARALLEL_LARGE_ENGRAVER_RECIPES = new ParallelRecipeMap(LARGE_ENGRAVER_RECIPES);
        PARALLEL_LARGE_MIXER_RECIPES = new ParallelRecipeMap(LARGE_MIXER_RECIPES);
        PARALLEL_PACKER_RECIPES = new ParallelRecipeMap(PACKER_RECIPES);
        PARALLEL_UNPACKER_RECIPES = new ParallelRecipeMap(UNPACKER_RECIPES);
        PARALLEL_WIREMILL_RECIPES = new ParallelRecipeMap(WIREMILL_RECIPES);
        PARALLEL_PLASMA_CONDENSER_RECIPES = new ParallelRecipeMap(PLASMA_CONDENSER_RECIPES);
        PARALLEL_BLAST_ALLOY_RECIPES = new ParallelRecipeMap(BLAST_ALLOY_RECIPES);
        PARALLEL_BLAST_RECIPES = new ParallelRecipeMap(BLAST_RECIPES);
        PARALLEL_VACUUM_RECIPES = new ParallelRecipeMap(VACUUM_RECIPES);
    }
}
