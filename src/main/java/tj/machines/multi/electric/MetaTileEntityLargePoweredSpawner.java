package tj.machines.multi.electric;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.capabilities.impl.GAMultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.TJRecipeMaps;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJGARecipeMapMultiblockControllerBase;
import tj.textures.TJTextures;

import javax.annotation.Nonnull;

public class MetaTileEntityLargePoweredSpawner extends TJGARecipeMapMultiblockControllerBase {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityLargePoweredSpawner(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.LARGE_POWERED_SPAWNER_RECIPES, false, true, false);
        this.recipeMapWorkable = new GAMultiblockRecipeLogic(this);
    }

    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargePoweredSpawner(this.metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .aisle("CCCCC", "~TMT~", "~TMT~", "~TMT~", "CGGGC")
                .aisle("CCCCC", "~MEM~", "~MEM~", "~MEM~", "CGGGC")
                .aisle("CCCCC", "~TMT~", "~TMT~", "~TMT~", "CGGGC")
                .aisle("CCSCC", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .where('S', selfPredicate())
                .where('C', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)).or(blockPredicate(Block.getBlockFromName("contenttweaker:soulcasing"))))
                .where('F', blockPredicate(Block.getBlockFromName("gregtech:frame_protactinium")))
                .where('T', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('M', blockPredicate(Block.getBlockFromName("enderio:block_decoration1")))
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

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }
}
