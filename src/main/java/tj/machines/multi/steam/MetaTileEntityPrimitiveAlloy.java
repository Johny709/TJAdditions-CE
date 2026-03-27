package tj.machines.multi.steam;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.recipes.Recipe;
import tj.TJRecipeMaps;
import tj.TJValues;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMachineCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.capability.OverclockManager;


public class MetaTileEntityPrimitiveAlloy extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = new MultiblockAbility[]{MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_ITEMS};

    public MetaTileEntityPrimitiveAlloy(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.PRIMITIVE_ALLOY_RECIPES, false, true);
        this.recipeLogic.setAllowOverclocking(false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityPrimitiveAlloy(this.metaTileEntityId);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setParallel(1);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "BBB")
                .aisle("XXX", "X#X", "B#B")
                .aisle("XXX", "XSX", "BBB")
                .where('S', selfPredicate())
                .where('X', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('B', statePredicate(MetaBlocks.MACHINE_CASING.getState(BlockMachineCasing.MachineCasingType.BRONZE_HULL)))
                .where('#', isAirPredicate())
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PRIMITIVE_BRICKS;
    }

    @Override
    public int getEUtMultiplier() {
        return 0;
    }

    @Override
    public int getParallel() {
        return 0; // don't display parallel overclocking per tier on tooltip
    }

    @Override
    public boolean renderTJLogoOverlay() {
        return true;
    }

    @Override
    public IEnergyContainer getInputEnergyContainer() {
        return TJValues.DUMMY_ENERGY;
    }

    @Override
    public boolean usesEnergy() {
        return false;
    }
}
