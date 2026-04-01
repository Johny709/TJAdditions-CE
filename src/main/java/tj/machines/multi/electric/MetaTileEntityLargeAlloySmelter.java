package tj.machines.multi.electric;

import gregicadditions.GAConfig;
import gregicadditions.GAValues;
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
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static gregicadditions.item.GAMetaBlocks.METAL_CASING_1;

public class MetaTileEntityLargeAlloySmelter extends TJRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};

    public MetaTileEntityLargeAlloySmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.ALLOY_SMELTER_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity (MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeAlloySmelter(metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.large_alloy_smelter.description"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int tier = 0;
        BlockWireCoil.CoilType coilType;
        GAHeatingCoil.CoilType gaCoilType;
        if ((coilType = context.getOrDefault("coilType", BlockWireCoil.CoilType.CUPRONICKEL)) != null)
            tier = coilType.ordinal() + 1;
        else if ((gaCoilType = context.getOrDefault("gaCoilType", GAHeatingCoil.CoilType.TITAN_STEEL_COIL)) != null)
            tier = gaCoilType.ordinal() + 8;
        if (tier < GAValues.MAX) {
            this.maxVoltage = 8L << tier * 2;
            this.tier = tier;
        }
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

    @Override
    public int getEUtMultiplier() {
        return TJConfig.largeAlloySmelter.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.largeAlloySmelter.durationPercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.largeAlloySmelter.stack;
    }
}
