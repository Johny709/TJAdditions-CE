package tj.machines.multi.electric;

import gregicadditions.GAConfig;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAHeatingCoil;
import gregicadditions.item.metal.MetalCasing1;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockWireCoil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import tj.TJConfig;
import tj.builder.multicontrollers.TJLargeSimpleRecipeMapMultiblockControllerBase;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Predicate;

import static gregicadditions.item.GAMetaBlocks.METAL_CASING_1;

public class MetaTileEntityLargeAlloySmelter extends TJLargeSimpleRecipeMapMultiblockControllerBase {

    private int tier;

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityLargeAlloySmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.ALLOY_SMELTER_RECIPES, TJConfig.largeAlloySmelter.eutPercentage, TJConfig.largeAlloySmelter.durationPercentage, TJConfig.largeAlloySmelter.chancePercentage, TJConfig.largeAlloySmelter.stack);
    }

    @Override
    public MetaTileEntity createMetaTileEntity (MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeAlloySmelter(metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int temperature = context.getOrDefault("blastFurnaceTemperature", 0);

        switch (temperature) {
            case 2700:
                tier = 2;
                break;
            case 3600:
                tier = 3;
                break;
            case 4500:
                tier = 4;
                break;
            case 5400:
                tier = 5;
                break;
            case 7200:
                tier = 6;
                break;
            case 8600:
                tier = 7;
                break;
            case 9600:
                tier = 8;
                break;
            case 10700:
                tier = 9;
                break;
            case 11200:
                tier = 10;
                break;
            case 12600:
                tier = 11;
                break;
            case 14200:
                tier = 12;
                break;
            case 28400:
                tier = 13;
                break;
            case 56800:
                tier = 14;
                break;
            default:
                tier = 1;
        }
        maxVoltage = (long) (Math.pow(4, tier) * 8);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.tier = 0;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("HHHHH", "HHHHH", "HHHHH", "~H~H~")
                .aisle("HHHHH", "HcCcH", "c#c#c", "~c~c~")
                .aisle("HHHHH", "HcCcH", "c#c#c", "~c~c~")
                .aisle("HHHHH", "HcCcH", "c#c#c", "~c~c~")
                .aisle("HHHHH", "HHSHH", "HHHHH", "~H~H~")
                .setAmountAtLeast('L', 15)
                .where('S', selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(getCasingState()))
                .where('H', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('c', heatingCoilPredicate().or(heatingCoilPredicate2()))
                .where('#', isAirPredicate())
                .where('~', (tile) -> true)
                .build();

    }

    public static Predicate<BlockWorldState> heatingCoilPredicate() {
        return blockWorldState -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (!(blockState.getBlock() instanceof BlockWireCoil))
                return false;
            BlockWireCoil blockWireCoil = (BlockWireCoil) blockState.getBlock();
            BlockWireCoil.CoilType coilType = blockWireCoil.getState(blockState);
            if (Arrays.asList(GAConfig.multis.heatingCoils.gtceHeatingCoilsBlacklist).contains(coilType.getName()))
                return false;

            int blastFurnaceTemperature = coilType.getCoilTemperature();
            int currentTemperature = blockWorldState.getMatchContext().getOrPut("blastFurnaceTemperature", blastFurnaceTemperature);

            BlockWireCoil.CoilType currentCoilType = blockWorldState.getMatchContext().getOrPut("coilType", coilType);

            return currentTemperature == blastFurnaceTemperature && coilType.equals(currentCoilType);
        };
    }

    public static Predicate<BlockWorldState> heatingCoilPredicate2() {
        return blockWorldState -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (!(blockState.getBlock() instanceof GAHeatingCoil))
                return false;
            GAHeatingCoil blockWireCoil = (GAHeatingCoil) blockState.getBlock();
            GAHeatingCoil.CoilType coilType = blockWireCoil.getState(blockState);
            if (Arrays.asList(GAConfig.multis.heatingCoils.gregicalityheatingCoilsBlacklist).contains(coilType.getName()))
                return false;

            int blastFurnaceTemperature = coilType.getCoilTemperature();
            int currentTemperature = blockWorldState.getMatchContext().getOrPut("blastFurnaceTemperature", blastFurnaceTemperature);

            GAHeatingCoil.CoilType currentCoilType = blockWorldState.getMatchContext().getOrPut("gaCoilType", coilType);

            return currentTemperature == blastFurnaceTemperature && coilType.equals(currentCoilType);
        };
    }

    public IBlockState getCasingState() {
        return METAL_CASING_1.getState(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.ZIRCONIUM_CARBIDE_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

}
