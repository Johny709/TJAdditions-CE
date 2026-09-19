package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.ConveyorCasing;
import gregicadditions.item.components.RobotArmCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import tj.TJConfig;
import tj.builder.multicontrollers.ExtendableMultiblockController;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.integration.jei.multi.parallel.IParallelMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class LargeChiselWorkbenchInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public ExtendableMultiblockController getController() {
        return TJMetaTileEntities.LARGE_CHISEL_WORKBENCH;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {

        final TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, UP, LEFT);
        for (int layer = 0; layer < extent; layer++) {
            String power = layer == 0 ? "CEC" : "CCC";
            builder.aisle(power, "CCC", "C~C", "C~C");
            builder.aisle("CCC", "CcC", "###", "CrC");
        }
        return builder.aisle("CMC", "ISO", "C~C", "C~C")
            .where('S', this.getController(), EnumFacing.WEST)
            .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.MARAGING_STEEL_250))
            .where('c', PlaceholderType.CONVEYOR, GAMetaBlocks.CONVEYOR_CASING.getState(ConveyorCasing.CasingType.values()[0]))
            .where('r', PlaceholderType.ROBOT_ARM, GAMetaBlocks.ROBOT_ARM_CASING.getState(RobotArmCasing.CasingType.values()[0]))
            .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
            .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
            .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
            .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
            .build();
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.MARAGING_STEEL_250), new TextComponentTranslation("gregtech.multiblock.preview.limit", 12)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.CONVEYOR_CASING.getItemVariant(ConveyorCasing.CasingType.CONVEYOR_LV), COMPONENT_BLOCK_TOOLTIP);
        this.addBlockTooltip(GAMetaBlocks.ROBOT_ARM_CASING.getItemVariant(RobotArmCasing.CasingType.ROBOT_ARM_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.large_chisel_workbench.description")};
    }
}
