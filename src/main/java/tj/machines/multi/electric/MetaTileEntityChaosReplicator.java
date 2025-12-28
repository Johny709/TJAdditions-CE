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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.TJRecipeMaps;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJGARecipeMapMultiblockControllerBase;
import tj.textures.TJTextures;

import javax.annotation.Nonnull;

public class MetaTileEntityChaosReplicator extends TJGARecipeMapMultiblockControllerBase {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = new MultiblockAbility[]{MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityChaosReplicator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.CHAOS_REPLICATOR_RECIPES, false, true, false);
        this.recipeMapWorkable = new GAMultiblockRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityChaosReplicator(this.metaTileEntityId);/*(3)!*/
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start() /*(4)!*/
                .aisle("CCCCCCC", "CHHHHHC", "CQQCQQC", "CQQCQQC", "CQQCQQC", "CQQQQQC", "CQQQQQC", "CHHHHHC", "CCCCCCC")
                .aisle("CCCCCCC", "HDDDDDH", "QF~~~FQ", "QF~~~FQ", "QF~A~FQ", "QF~~~FQ", "QF~~~FQ", "HDDDDDH", "CCCCCCC")
                .aisle("CCCCCCC", "HDDDDDH", "Q~DDD~Q", "Q~~~~~Q", "Q~~~~~Q", "Q~~~~~Q", "Q~DDD~Q", "HDDDDDH", "CCCCCCC")
                .aisle("CCCCCCC", "HDDDDDH", "C~DDD~C", "C~~D~~C", "CA~R~AC", "Q~~D~~Q", "Q~DDD~Q", "HDDDDDH", "CCCCCCC")
                .aisle("CCCCCCC", "HDDDDDH", "Q~DDD~Q", "Q~~~~~Q", "Q~~~~~Q", "Q~~~~~Q", "Q~DDD~Q", "HDDDDDH", "CCCCCCC")
                .aisle("CCCCCCC", "HDDDDDH", "QF~~~FQ", "QF~~~FQ", "QF~A~FQ", "QF~~~FQ", "QF~~~FQ", "HDDDDDH", "CCCCCCC")
                .aisle("CCCCCCC", "CHHSHHC", "CQQCQQC", "CQQCQQC", "CQQCQQC", "CQQQQQC", "CQQQQQC", "CHHHHHC", "CCCCCCC")
                .where('S', selfPredicate())
                .where('H', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)).or(blockPredicate(getCasingAlternativeState())))
                .where('C', statePredicate(getCasingState()).or(blockPredicate(getCasingAlternativeState())))
                .where('F', blockPredicate(Block.getBlockFromName("gregtech:frame_enriched_naquadah_alloy")))
                .where('D', blockPredicate(Block.getBlockFromName("draconicevolution:infused_obsidian")))
                .where('Q', blockPredicate(Block.getBlockFromName("enderio:block_fused_quartz")))
                .where('A', blockPredicate(Block.getBlockFromName("draconicevolution:draconic_block")))
                .where('R', blockPredicate(Block.getBlockFromName("gregtech:frame_chaos")))
                .where('~', isAirPredicate())
                .build();
    }

    protected IBlockState getCasingState() {
        return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.CHOATIC_CASING);
    }
    protected Block getCasingAlternativeState() {
        return Block.getBlockFromName("contenttweaker:chaoticcasing");
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.CHOATIC;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }
}
