package tj.machines.multi.electric;

import gregtech.api.recipes.Recipe;
import tj.TJRecipeMaps;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.textures.TJTextures;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;


public class MetaTileEntityLargeVialProcessor extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityLargeVialProcessor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.LARGE_VIAL_PROCESSOR_RECIPES);
    }

    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeVialProcessor(this.metaTileEntityId);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setParallel(1);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .aisle("CTTTC", "~BGB~", "~BGB~", "~BGB~", "CTTTC")
                .aisle("CTETC", "~GEG~", "~GEG~", "~GEG~", "CTETC")
                .aisle("CTTTC", "~BGB~", "~BGB~", "~BGB~", "CTTTC")
                .aisle("CCSCC", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .where('S', selfPredicate())
                .where('C', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)).or(blockPredicate(Block.getBlockFromName("contenttweaker:soulcasing"))))
                .where('F', blockPredicate(Block.getBlockFromName("gregtech:frame_protactinium")))
                .where('T', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('B', statePredicate(GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TUNGSTENSTEEL_GEARBOX_CASING)))
                .where('E', state -> state.getBlockState() == Block.getBlockFromName("enderio:block_alloy").getStateFromMeta(8))
                .where('G', statePredicate(MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('~', state -> true)
                .build();
    }

    protected IBlockState getCasingState() {
        return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.SOUL_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.SOUL;
    }
}
