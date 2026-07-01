package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.machines.GATileEntities;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.TJValues;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.multi.electric.MetaTileEntityProcessingArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class ProcessingArrayInfo extends TJMultiblockInfoPage {

    private final MetaTileEntityProcessingArray processingArray;

    public ProcessingArrayInfo(MetaTileEntityProcessingArray processingArray) {
        this.processingArray = processingArray;
    }

    @Override
    public MetaTileEntityProcessingArray getController() {
        return this.processingArray;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("CCC", "CEC", "CCC")
                .aisle("CCC", "CFC", "CCC")
                .aisle("CCC", "ISO", "iMo")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', this.getController().getCasingState())
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                    .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                    .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.WEST)
                    .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], EnumFacing.WEST)
                    .where('F', PlaceholderType.FRAMEWORK,this.getVoltageCasing(0))
                    .build();

    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(this.getController().getCasingItem(), new TextComponentTranslation("gregtech.multiblock.preview.limit", 5)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        Arrays.stream(GAMultiblockCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MUTLIBLOCK_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(GAMultiblockCasing2.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MUTLIBLOCK_CASING2.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.processing_array.description"),
                        I18n.format("tj.multiblock.processing_array.eut")},
                ArrayUtils.addAll(super.getDescription(), I18n.format("tj.multiblock.universal.tooltip.2", TJValues.thousandFormat.format(this.getController().getMaxParallel()))));
    }
}
