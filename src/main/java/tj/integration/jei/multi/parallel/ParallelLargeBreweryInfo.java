package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.MetaBlocks;
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
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;
import tj.machines.multi.parallel.MetaTileEntityParallelLargeBrewery;

import java.util.ArrayList;
import java.util.List;

import static gregicadditions.GAMaterials.Grisium;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;


public class ParallelLargeBreweryInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelLargeBrewery getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_BREWERY;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {

        final TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, UP, LEFT);
        builder.aisle("CCCCC", "F~C~F", "CCECC", "CCMCC", "CCCCC", "~CCC~");
        for (int layer = 0; layer < extent; layer++) {
            builder.aisle("~CCC~", "~~C~~", "~G~G~", "C~P~C", "~G~G~", "~~C~~");
            builder.aisle("~CCC~", "~~C~~", "~G~G~", "p~P~p", "~G~G~", "~~C~~");
            builder.aisle("~CCC~", "~~C~~", "~G~G~", "C~P~C", "~G~G~", "~~C~~");
        }
        return builder.aisle("CCCCC", "F~C~F", "CiSoC", "CIMOC", "CCmCC", "~CCC~").where('S', getController(), EnumFacing.WEST)
                    .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.GRISIUM))
                    .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.OSMIRIDIUM_GLASS))
                    .where('P', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.PTFE_PIPE))
                    .where('M', PlaceholderType.MOTOR , GAMetaBlocks.MOTOR_CASING.getState(MotorCasing.CasingType.values()[0]))
                    .where('p', PlaceholderType.PUMP,GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[0]))
                    .where('F', MetaBlocks.FRAMES.get(Grisium).getDefaultState())
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                    .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                    .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.WEST)
                    .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], EnumFacing.WEST)
                    .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                    .where('m', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                    .build();

    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.GRISIUM), new TextComponentTranslation("gregtech.multiblock.preview.limit", 0)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.MOTOR_CASING.getItemVariant(MotorCasing.CasingType.MOTOR_LV), COMPONENT_BLOCK_TOOLTIP);
        this.addBlockTooltip(GAMetaBlocks.PUMP_CASING.getItemVariant(PumpCasing.CasingType.PUMP_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_large_brewery.description"),
                I18n.format("tj.multiblock.parallel.description")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
