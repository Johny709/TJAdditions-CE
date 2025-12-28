package tj.machines.multi.electric;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.FieldGenCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.machines.multi.nuclear.MetaTileEntityNuclearReactor;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.TJConfig;
import tj.builder.multicontrollers.TJLargeSimpleRecipeMapMultiblockControllerBase;

import javax.annotation.Nonnull;

public class MetaTileEntityLargeDecayChamber extends TJLargeSimpleRecipeMapMultiblockControllerBase {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_FLUIDS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityLargeDecayChamber(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.DECAY_CHAMBERS_RECIPES, TJConfig.decayChamber.eutPercentage, TJConfig.decayChamber.durationPercentage, TJConfig.decayChamber.chancePercentage, TJConfig.decayChamber.stack);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeDecayChamber(this.metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~~C~~", "~HHH~", "CHHHC", "~HHH~", "~~C~~")
                .aisle("~HHH~", "C###C", "C#F#C", "C###C", "~HHH~")
                .aisle("CHHHC", "C#F#C", "CFRFC", "C#F#C", "CHHHC")
                .aisle("~HHH~", "C###C", "C#F#C", "C###C", "~HHH~")
                .aisle("~~C~~", "~HHH~", "CHSHC", "~HHH~", "~~C~~")
                .setAmountAtLeast('L', 24)
                .where('S', selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(this.getCasingState()))
                .where('H', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('R', MetaTileEntityNuclearReactor.heatingCoilPredicate())
                .where('F', fieldGenPredicate())
                .where('#', isAirPredicate())
                .where('~', (tile) -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.LEAD);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.LEAD_CASING;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int min = context.getOrDefault("FieldGen", FieldGenCasing.CasingType.FIELD_GENERATOR_LV).getTier();
        this.maxVoltage = (long) (Math.pow(4, min) * 8);
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return ClientHandler.REPLICATOR_OVERLAY;
    }
}
