package tj.machines.multi.electric;

import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.machines.multi.nuclear.MetaTileEntityNuclearReactor;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.TJConfig;
import tj.builder.multicontrollers.TJMultiRecipeMapMultiblockController;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static tj.machines.multi.electric.MetaTileEntityLargeGreenhouse.glassPredicate;

public class MetaTileEntityLargeNuclearReactor extends TJMultiRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};

    public MetaTileEntityLargeNuclearReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.NUCLEAR_REACTOR_RECIPES, GARecipeMaps.NUCLEAR_BREEDER_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeNuclearReactor(this.metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~XXX~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~XXX~")
                .aisle("XXXXX", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "XXXXX")
                .aisle("XXXXX", "GNNNG", "GNNNG", "GNNNG", "GNNNG", "GNNNG", "GNNNG", "GNNNG", "XXXXX")
                .aisle("XXXXX", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "XXXXX")
                .aisle("~XSX~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~XXX~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('G', glassPredicate())
                .where('N', MetaTileEntityNuclearReactor.heatingCoilPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.CLADDED_REACTOR_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.CLADDED_REACTOR_CASING;
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.largeNuclearReactor.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.largeNuclearReactor.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.largeNuclearReactor.chancePercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.largeNuclearReactor.stack;
    }
}
