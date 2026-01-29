package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockBoilerCasing;
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

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;


public class ParallelLargeElectrolyzerInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public ParallelRecipeMapMultiblockController getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_ELECTROLYZER;
    }

    @Override
    public List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes) {
        List<TJMultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        int size = Math.min(TJConfig.machines.maxLayersInJEI, this.getController().getMaxParallel());
        for (int shapeInfo = 1; shapeInfo <= size; shapeInfo++) {
            TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, UP, LEFT);
            for (int layer = 0; layer < shapeInfo; layer++) {

                String energyE = layer == 0 ? "CEGCC" : "CCGCC";

                builder.aisle(energyE, "CCGCC", "CCGCC", "CC#CC");
                builder.aisle("CCGCC", "CP#MC", "CCGCC", "C###C");
            }
            builder.aisle("CimoC", "CISOC", "CCGCC", "CC#CC");
            TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            for (int tier = 0; tier < infos.length; tier++) {
                infos[tier] = builder.where('S', getController(), EnumFacing.WEST)
                        .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.POTIN))
                        .where('G', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE))
                        .where('P', GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .where('M', GAMetaBlocks.MOTOR_CASING.getState(MotorCasing.CasingType.values()[Math.max(0, tier - 1)]))
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
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.POTIN), new TextComponentTranslation("gregtech.multiblock.preview.limit", 14)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.PUMP_CASING.getItemVariant(PumpCasing.CasingType.PUMP_LV), COMPONENT_BLOCK_TOOLTIP);
        this.addBlockTooltip(GAMetaBlocks.MOTOR_CASING.getItemVariant(MotorCasing.CasingType.MOTOR_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_large_electrolyzer.description"),
                I18n.format("tj.multiblock.parallel.description"),
                I18n.format("tj.multiblock.parallel.extend.tooltip")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
