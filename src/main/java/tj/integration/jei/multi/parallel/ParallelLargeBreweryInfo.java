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
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregicadditions.GAMaterials.Grisium;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;


public class ParallelLargeBreweryInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public ParallelRecipeMapMultiblockController getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_BREWERY;
    }

    @Override
    public List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes) {
        List<TJMultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        int size = Math.min(TJConfig.machines.maxLayersInJEI, this.getController().getMaxParallel());
        for (int shapeInfo = 1; shapeInfo <= size; shapeInfo++) {
            TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, UP, LEFT);
            builder.aisle("CCCCC", "F~C~F", "CCECC", "CCMCC", "CCCCC", "~CCC~");
            for (int layer = 0; layer < shapeInfo; layer++) {
                builder.aisle("~CCC~", "~~C~~", "~G~G~", "C~P~C", "~G~G~", "~~C~~");
                builder.aisle("~CCC~", "~~C~~", "~G~G~", "p~P~p", "~G~G~", "~~C~~");
                builder.aisle("~CCC~", "~~C~~", "~G~G~", "C~P~C", "~G~G~", "~~C~~");
            }
            builder.aisle("CCCCC", "F~C~F", "CiSoC", "CIMOC", "CCmCC", "~CCC~");
            TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            for (int tier = 0; tier < infos.length; tier++) {
                infos[tier] = builder.where('S', getController(), EnumFacing.WEST)
                        .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.GRISIUM))
                        .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.OSMIRIDIUM_GLASS))
                        .where('P', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.PTFE_PIPE))
                        .where('M', GAMetaBlocks.MOTOR_CASING.getState(MotorCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .where('p', GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .where('F', MetaBlocks.FRAMES.get(Grisium).getDefaultState())
                        .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.WEST)
                        .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.WEST)
                        .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.WEST)
                        .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.WEST)
                        .where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                        .where('m', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
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
                I18n.format("tj.multiblock.parallel.description"),
                I18n.format("tj.multiblock.parallel.extend.tooltip")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
