package tj.integration.jei;

import gregicadditions.item.GAHeatingCoil;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import tj.TJValues;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;

import java.util.Collections;
import java.util.List;


public abstract class TJMultiblockInfoPage extends MultiblockInfoPage {

    protected static final ITextComponent COMPONENT_BLOCK_TOOLTIP = new TextComponentTranslation("gregtech.multiblock.universal.component_casing.tooltip").setStyle(new Style().setColor(TextFormatting.RED));
    protected static final ITextComponent COMPONENT_TIER_ANY_TOOLTIP = new TextComponentTranslation("tj.multiblock.component_casing.any.tooltip").setStyle(new Style().setColor(TextFormatting.GREEN));

    protected IBlockState getVoltageCasing(int tier) {
        switch (tier) {
            case 1: return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TIERED_HULL_LV);
            case 2: return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TIERED_HULL_MV);
            case 3: return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TIERED_HULL_HV);
            case 4: return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TIERED_HULL_EV);
            case 5: return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TIERED_HULL_IV);
            case 6: return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TIERED_HULL_LUV);
            case 7: return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TIERED_HULL_ZPM);
            case 8: return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TIERED_HULL_UV);
            case 9: return GAMetaBlocks.MUTLIBLOCK_CASING2.getState(GAMultiblockCasing2.CasingType.TIERED_HULL_UHV);
            case 10: return GAMetaBlocks.MUTLIBLOCK_CASING2.getState(GAMultiblockCasing2.CasingType.TIERED_HULL_UEV);
            case 11: return GAMetaBlocks.MUTLIBLOCK_CASING2.getState(GAMultiblockCasing2.CasingType.TIERED_HULL_UIV);
            case 12: return GAMetaBlocks.MUTLIBLOCK_CASING2.getState(GAMultiblockCasing2.CasingType.TIERED_HULL_UMV);
            case 13: return GAMetaBlocks.MUTLIBLOCK_CASING2.getState(GAMultiblockCasing2.CasingType.TIERED_HULL_UXV);
            case 14: return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TIERED_HULL_MAX);
            default: return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TIERED_HULL_ULV);
        }
    }

    protected MetaTileEntity getEnergyHatch(int tier, boolean isOutput) {
        switch (tier) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8: return isOutput ? MetaTileEntities.ENERGY_OUTPUT_HATCH[tier] : MetaTileEntities.ENERGY_INPUT_HATCH[tier];
            case 9:
            case 10:
            case 11:
            case 12:
            case 13: return isOutput ? GATileEntities.ENERGY_OUTPUT[tier - 9] : GATileEntities.ENERGY_INPUT[tier - 9];
            case 14: return isOutput ? MetaTileEntities.ENERGY_OUTPUT_HATCH[9] : MetaTileEntities.ENERGY_INPUT_HATCH[9];
            default: return isOutput ? MetaTileEntities.ENERGY_OUTPUT_HATCH[0] : MetaTileEntities.ENERGY_INPUT_HATCH[0];
        }
    }

    public IBlockState getCoils(int tier) {
        switch (tier) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7: return MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.values()[tier - 1]);
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14: return GAMetaBlocks.HEATING_COIL.getState(GAHeatingCoil.CoilType.values()[tier - 8]);
            default: return MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.CUPRONICKEL);
        }
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Collections.emptyList();
    }

    @Override
    public String[] getDescription() {
        if (!(this.getController() instanceof ParallelRecipeMapMultiblockController))
            return new String[]{""};
        ParallelRecipeMapMultiblockController controller = (ParallelRecipeMapMultiblockController) this.getController();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < controller.getRecipeMaps().length; i++) {
            builder.append(controller.getRecipeMaps()[i].getLocalizedName());
            if (i < controller.getRecipeMaps().length - 1)
                builder.append(", ");
        }
        return new String[]{I18n.format("gtadditions.multiblock.universal.tooltip.1", builder.toString()),
                I18n.format("gtadditions.multiblock.universal.tooltip.2", TJValues.thousandTwoPlaceFormat.format(controller.getEUPercentage() / 100.0)),
                I18n.format("gtadditions.multiblock.universal.tooltip.3", TJValues.thousandTwoPlaceFormat.format(controller.getDurationPercentage() / 100.0)),
                I18n.format("tj.multiblock.parallel.tooltip.1", controller.getStack()).replace("§7", "§0").replace("§r", "§r§0"),
                I18n.format("tj.multiblock.parallel.tooltip.2", controller.getMaxParallel()),
                I18n.format("gtadditions.multiblock.universal.tooltip.5", controller.getChancePercentage())};
    }
}
