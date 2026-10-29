package tj.integration.jei.multi.electric;

import gregicadditions.channels.GAChannelRegistry;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.RobotArmCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.BlockInfo;
import gregtech.common.channels.ChannelRegistry;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class LargeSuperFisherInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_SUPER_FISHER;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int i) {
        return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("~~CCCCC~~", "~~CEMCC~~", "~~C~~~C~~", "~~C~~~C~~", "~~C~~~C~~", "~~~~~~~~~", "~~~~~~~~~")
                .aisle("~CCCCCCC~", "~C~~~~~C~", "~C~~~~~C~", "~C~~~~~C~", "~C~~~~~C~", "~~C~~~C~~", "~~~~~~~~~")
                .aisle("CCWWWWWCC", "C~~~~~~~C", "C~~~~~~~C", "C~~~~~~~C", "C~~~~~~~C", "~C~~~~~C~", "~~C~~~C~~")
                .aisle("CCWWWWWCC", "C~~###~~C", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~CCC~~~")
                .aisle("CCWWWWWCC", "C~~###~~C", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~CRC~~~")
                .aisle("CCWWWWWCC", "C~~###~~C", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~CCC~~~")
                .aisle("CCWWWWWCC", "C~~~~~~~C", "C~~~~~~~C", "C~~~~~~~C", "C~~~~~~~C", "~C~~~~~C~", "~~C~~~C~~")
                .aisle("~CCCCCCC~", "~C~~~~~C~", "~C~~~~~C~", "~C~~~~~C~", "~C~~~~~C~", "~~C~~~C~~", "~~~~~~~~~")
                .aisle("~~CCCCC~~", "~~CISOC~~", "~~C~~~C~~", "~~C~~~C~~", "~~C~~~C~~", "~~~~~~~~~", "~~~~~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.GRISIUM))
                .where('W', new BlockInfo(Blocks.WATER))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.EAST)
                .where('R', GAChannelRegistry.ROBOT_ARM, GAMetaBlocks.ROBOT_ARM_CASING.getState(RobotArmCasing.CasingType.ROBOT_ARM_LV))
                .where('I', ChannelRegistry.INPUT_BUS, MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                .where('O', ChannelRegistry.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                .where('M', ChannelRegistry.ENERGY_INPUT_HATCH, MetaTileEntities.ENERGY_INPUT_HATCH[0], EnumFacing.EAST)
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gtadditions.multiblock.universal.tooltip.4", TJConfig.largeNamingMachine.stack),
                I18n.format("gregtech.machine.fisher.requirement", (int) Math.sqrt(25))};
    }
}
