package tj.machines.multi.steam;

import gregtech.api.recipes.Recipe;
import tj.TJRecipeMaps;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import gregicadditions.item.metal.MetalCasing1;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.capability.OverclockManager;

import static gregicadditions.client.ClientHandler.ZIRCONIUM_CARBIDE_CASING;
import static gregicadditions.item.GAMetaBlocks.METAL_CASING_1;


public class MetaTileEntityHeatExchanger extends TJRecipeMapMultiblockController {
    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = new MultiblockAbility[]{MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_FLUIDS};

    public MetaTileEntityHeatExchanger (ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.HEAT_EXCHANGER_RECIPES, false, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityHeatExchanger(this.metaTileEntityId);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setParallel(1);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("FFF", "FFF", "FFF")
                .aisle("FFF", "F#F", "FFF")
                .aisle("FFF", "FSF", "FFF")
                .where('S', selfPredicate())
                .where('F', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('#', isAirPredicate())
                .build();
    }

    protected IBlockState getCasingState() {
        return METAL_CASING_1.getState(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ZIRCONIUM_CARBIDE_CASING;
    }
}
