package tj.machines.multi.electric;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.TJRecipeMaps;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.textures.TJTextures;


public class MetaTileEntityChaosReplicator extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = new MultiblockAbility[]{MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityChaosReplicator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, TJRecipeMaps.CHAOS_REPLICATOR_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityChaosReplicator(this.metaTileEntityId);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setParallel(1);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
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
}
