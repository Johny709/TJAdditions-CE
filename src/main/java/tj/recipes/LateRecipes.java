package tj.recipes;

import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import net.minecraft.util.ResourceLocation;
import tj.TJRecipeMaps;

import java.util.Optional;

import static tj.machines.TJMetaTileEntities.MEGA_COKE_OVEN;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;

public class LateRecipes {

    public static void init() {
        MetaTileEntity cokeOven = Optional.ofNullable(GregTechAPI.META_TILE_ENTITY_REGISTRY.getObject(new ResourceLocation("multiblocktweaker", "coke_oven_2"))).orElseThrow(() -> new NullPointerException("MTE coke oven not found"));
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(cokeOven.getStackForm(64))
                .inputs(cokeOven.getStackForm(64))
                .inputs(cokeOven.getStackForm(64))
                .inputs(cokeOven.getStackForm(64))
                .outputs(MEGA_COKE_OVEN.getStackForm(1))
                .EUt(30)
                .duration(1200)
                .buildAndRegister();
        TJRecipeMaps.COKE_OVEN_RECIPES = ((RecipeMapMultiblockController) cokeOven).recipeMap;
        TJRecipeMaps.PRIMITIVE_ALLOY_RECIPES = (((RecipeMapMultiblockController) Optional.ofNullable(GregTechAPI.META_TILE_ENTITY_REGISTRY.getObject(new ResourceLocation("multiblocktweaker", "primitive_alloy"))).orElseThrow(() -> new NullPointerException("MTE for recipe map not found")))).recipeMap;
        TJRecipeMaps.HEAT_EXCHANGER_RECIPES = ((RecipeMapMultiblockController) Optional.ofNullable(GregTechAPI.META_TILE_ENTITY_REGISTRY.getObject(new ResourceLocation("multiblocktweaker", "heat_exchanger"))).orElseThrow(() -> new NullPointerException("MTE for recipe map not found"))).recipeMap;
        TJRecipeMaps.ARMOR_INFUSER_RECIPES = ((RecipeMapMultiblockController) Optional.ofNullable(GregTechAPI.META_TILE_ENTITY_REGISTRY.getObject(new ResourceLocation("multiblocktweaker", "armor_infuser"))).orElseThrow(() -> new NullPointerException("MTE for recipe map not found"))).recipeMap;
        TJRecipeMaps.CHAOS_REPLICATOR_RECIPES = ((RecipeMapMultiblockController) Optional.ofNullable(GregTechAPI.META_TILE_ENTITY_REGISTRY.getObject(new ResourceLocation("multiblocktweaker", "chaos_replicator"))).orElseThrow(() -> new NullPointerException("MTE for recipe map not found"))).recipeMap;
        TJRecipeMaps.DRAGON_REPLICATOR_RECIPES = ((RecipeMapMultiblockController) Optional.ofNullable(GregTechAPI.META_TILE_ENTITY_REGISTRY.getObject(new ResourceLocation("multiblocktweaker", "dragon_egg_replicator"))).orElseThrow(() -> new NullPointerException("MTE for recipe map not found"))).recipeMap;
        TJRecipeMaps.LARGE_POWERED_SPAWNER_RECIPES = ((RecipeMapMultiblockController) Optional.ofNullable(GregTechAPI.META_TILE_ENTITY_REGISTRY.getObject(new ResourceLocation("multiblocktweaker", "large_powered_spawner"))).orElseThrow(() -> new NullPointerException("MTE for recipe map not found"))).recipeMap;
        TJRecipeMaps.LARGE_VIAL_PROCESSOR_RECIPES = ((RecipeMapMultiblockController) Optional.ofNullable(GregTechAPI.META_TILE_ENTITY_REGISTRY.getObject(new ResourceLocation("multiblocktweaker", "large_vial_processor"))).orElseThrow(() -> new NullPointerException("MTE for recipe map not found"))).recipeMap;
        TJRecipeMaps.registerLargeMachineRecipes(GARecipeMaps.STELLAR_FORGE_RECIPES, TJRecipeMaps.INTERSTELLAR_FORGE_RECIPES);
    }
}
