package tj.integration.jei;

import gregicadditions.Gregicality;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.util.ResourceLocation;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.integration.jei.recipe.GTRecipeTransferGuiHandler;
import tj.machines.multi.electric.MetaTileEntityAdvancedLargeChunkMiner;
import tj.machines.multi.steam.MetaTileEntityMegaBoiler;

import static tj.machines.TJMetaTileEntities.*;

@mezz.jei.api.JEIPlugin
public class TJJEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        TJMultiblockInfoCategory.registerRecipes(registry);

        registry.addRecipeCatalyst(INDUSTRIAL_STEAM_ENGINE.getStackForm(), INDUSTRIAL_STEAM_ENGINE.getRecipeUid());
        registry.addRecipeCatalyst(INFINITE_FLUID_DRILL.getStackForm(), Gregicality.MODID + ":drilling_rig");
        for (MetaTileEntityMegaBoiler boiler : MEGA_BOILER)
            registry.addRecipeCatalyst(boiler.getStackForm(), boiler.getRecipeUid());
        for (MetaTileEntityAdvancedLargeChunkMiner chunkMiner : ADVANCED_LARGE_CHUNK_MINERS)
            registry.addRecipeCatalyst(chunkMiner.getStackForm(), chunkMiner.getRecipeUid());

        for (ResourceLocation metaTileEntityId : GregTechAPI.META_TILE_ENTITY_REGISTRY.getKeys()) {
            MetaTileEntity metaTileEntity = GregTechAPI.META_TILE_ENTITY_REGISTRY.getObject(metaTileEntityId);
            if (metaTileEntity instanceof ParallelRecipeMapMultiblockController) {
                for (RecipeMap<?> recipeMap : ((ParallelRecipeMapMultiblockController) metaTileEntity).getRecipeMaps()) {
                    String recipeName = Gregicality.MODID + ":" + recipeMap.unlocalizedName;
                    registry.addRecipeCatalyst(metaTileEntity.getStackForm(), recipeName);
                    GTRecipeTransferGuiHandler gtRecipeTransferGuiHandler = new GTRecipeTransferGuiHandler(jeiHelpers.recipeTransferHandlerHelper());
                    registry.getRecipeTransferRegistry().addRecipeTransferHandler(gtRecipeTransferGuiHandler, recipeName);
                }
            }
        }

        TJMultiblockInfoCategory.getMultiblockRecipes().values().forEach(v -> {
            MultiblockInfoPage infoPage = v.getInfoPage();
            registry.addIngredientInfo(infoPage.getController().getStackForm(),
                    VanillaTypes.ITEM,
                    infoPage.getDescription());
        });
    }
}
