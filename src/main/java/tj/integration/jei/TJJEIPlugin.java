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
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.integration.jei.recipe.GTRecipeTransferGuiHandler;


@mezz.jei.api.JEIPlugin
public class TJJEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        final IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        TJMultiblockInfoCategory.registerRecipes(registry);

        for (ResourceLocation metaTileEntityId : GregTechAPI.META_TILE_ENTITY_REGISTRY.getKeys()) {
            final MetaTileEntity metaTileEntity = GregTechAPI.META_TILE_ENTITY_REGISTRY.getObject(metaTileEntityId);
            if (metaTileEntity instanceof ParallelRecipeMapMultiblockController) {
                for (RecipeMap<?> recipeMap : ((ParallelRecipeMapMultiblockController) metaTileEntity).getRecipeMaps()) {
                    final String recipeName = Gregicality.MODID + ":" + recipeMap.unlocalizedName;
                    registry.addRecipeCatalyst(metaTileEntity.getStackForm(), recipeName);
                    GTRecipeTransferGuiHandler gtRecipeTransferGuiHandler = new GTRecipeTransferGuiHandler(jeiHelpers.recipeTransferHandlerHelper());
                    registry.getRecipeTransferRegistry().addRecipeTransferHandler(gtRecipeTransferGuiHandler, recipeName);
                }
            } else if (metaTileEntity instanceof TJMultiblockControllerBase) {
                final String recipeUid = ((TJMultiblockControllerBase) metaTileEntity).getRecipeUid();
                if (recipeUid != null)
                    registry.addRecipeCatalyst(metaTileEntity.getStackForm(), ((TJMultiblockControllerBase) metaTileEntity).getRecipeUid());
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
