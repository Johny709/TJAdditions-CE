package tj.machines.multi.steam;

import tj.TJRecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;


public class MetaTileEntityMegaCokeOven extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = new MultiblockAbility[]{MultiblockAbility.EXPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_ITEMS};

    public MetaTileEntityMegaCokeOven (ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.COKE_OVEN_RECIPES, false, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMegaCokeOven(this.metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "CCCCCCCCC", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "CCCCCCCCC", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "CCCCCCCCC", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF", "C#C#C#C#C", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "FFFFSFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .setAmountAtLeast('L', 100)
                .where('S', selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('F', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('C', statePredicate(getCasingState()))
                .where('#', isAirPredicate())
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.COKE_BRICKS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.COKE_BRICKS;
    }

    @Override
    public int getParallel() {
        return 512;
    }

    @Override
    public int getTierDifference(long recipeEUt) {
        return 1;
    }
}
