package tj.machines.multi.electric;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.item.metal.MetalCasing1;
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
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.TJConfig;
import tj.builder.multicontrollers.TJLargeSimpleRecipeMapMultiblockControllerBase;

import javax.annotation.Nonnull;

public class MetaTileEntityLargeElectricImplosionCompressor extends TJLargeSimpleRecipeMapMultiblockControllerBase {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityLargeElectricImplosionCompressor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.ELECTRIC_IMPLOSION_RECIPES, TJConfig.largeElectricImplosionCompressor.eutPercentage, TJConfig.largeElectricImplosionCompressor.durationPercentage,
                TJConfig.largeElectricImplosionCompressor.chancePercentage, TJConfig.largeElectricImplosionCompressor.stack);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeElectricImplosionCompressor(this.metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "~~C~~", "~CCC~", "~CCC~", "~CCC~", "~~C~~", "XXXXX")
                .aisle("XPPPX", "~CCC~", "C###C", "C###C", "C###C", "~CCC~", "XPPPX")
                .aisle("XPpPX", "CCCCC", "C###C", "C###C", "C###C", "CCCCC", "XPMPX")
                .aisle("XPPPX", "~CCC~", "C###C", "C###C", "C###C", "~CCC~", "XPPPX")
                .aisle("XXSXX", "~~C~~", "~CCC~", "~CCC~", "~CCC~", "~~C~~", "XXXXX")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('p', pistonPredicate())
                .where('M', abilityPartPredicate(GregicAdditionsCapabilities.MUFFLER_HATCH))
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.INCOLOY_MA956);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int min = context.getOrDefault("Piston", PistonCasing.CasingType.PISTON_LV).getTier();
        this.maxVoltage = (long) (Math.pow(4, min) * 8);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.INCOLOY_MA956_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return ClientHandler.IMPLOSION_OVERLAY;
    }
}
