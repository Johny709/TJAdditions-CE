package tj.machines.multi.electric;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.item.components.PistonCasing;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.util.ResourceLocation;
import tj.TJConfig;
import tj.builder.multicontrollers.TJLargeSimpleRecipeMapMultiblockControllerBase;

import javax.annotation.Nonnull;

import static gregtech.api.unification.material.Materials.Steel;
import static tj.machines.multi.electric.MetaTileEntityLargeGreenhouse.glassPredicate;

public class MetaTileEntityLargeImplosionCompressor extends TJLargeSimpleRecipeMapMultiblockControllerBase {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityLargeImplosionCompressor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.IMPLOSION_RECIPES, TJConfig.largeImplosionCompressor.eutPercentage, TJConfig.largeImplosionCompressor.durationPercentage,
                TJConfig.largeImplosionCompressor.chancePercentage, TJConfig.largeImplosionCompressor.stack, true, true, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeImplosionCompressor(this.metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "F~~~F", "F~~~F", "F~~~F", "F~~~F", "F~~~F", "XXXXX")
                .aisle("XXXXX", "~PGP~", "~PGP~", "~PGP~", "~PGP~", "~PGP~", "XXXXX")
                .aisle("XXpXX", "~G#G~", "~G#G~", "~G#G~", "~G#G~", "~G#G~", "XXMXX")
                .aisle("XXXXX", "~PGP~", "~PGP~", "~PGP~", "~PGP~", "~PGP~", "XXXXX")
                .aisle("XXSXX", "F~~~F", "F~~~F", "F~~~F", "F~~~F", "F~~~F", "XXXXX")
                .where('S', this.selfPredicate())
                .where('X', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(Steel).getDefaultState()))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('p', pistonPredicate())
                .where('M', abilityPartPredicate(GregicAdditionsCapabilities.MUFFLER_HATCH))
                .where('G', glassPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int min = context.getOrDefault("Piston", PistonCasing.CasingType.PISTON_LV).getTier();
        this.maxVoltage = (long) (Math.pow(4, min) * 8);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.IMPLOSION_COMPRESSOR_OVERLAY;
    }
}
