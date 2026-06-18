package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.ConveyorCasing;
import gregicadditions.item.components.RobotArmCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;
import tj.machines.multi.electric.MetaTileEntityParallelCircuitAssemblyLine;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class ParallelCircuitAssemblyLineInfo extends TJMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelCircuitAssemblyLine getController() {
        return TJMetaTileEntities.PARALLEL_CIRCUIT_ASSEMBLY_LINE;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return new TJMultiblockShapeInfo.Builder(FRONT, UP, LEFT)
                .aisle("CCCCC", "GCOCG", "GC#CG", "EAeAE", "~EAE~")
                .aisle("FCICf", "G#c#G", "Gr#rG", "EAaAE", "~EAE~")
                .aisle("FCICf", "G#c#G", "Gr#rG", "EAaAE", "~EAE~")
                .aisle("FCICf", "G#c#G", "Gr#rG", "EAaAE", "~EAE~")
                .aisle("FCICf", "G#c#G", "Gr#rG", "EAaAE", "~EAE~")
                .aisle("FCICf", "G#c#G", "Gr#rG", "EAaAE", "~EAE~")
                .aisle("FCICM", "G#c#G", "Gr#rG", "EAaAE", "~EAE~")
                .aisle("CCCCC", "GCCCG", "GC#CG", "EASAE", "~EAE~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.SOUTH)
                .where('C', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.ASSEMBLER_CASING))
                .where('E', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                .where('a', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLER_CASING))
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                    .where('A', PlaceholderType.FRAMEWORK,this.getVoltageCasing(0))
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.DOWN)
                    .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.EAST)
                    .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.NORTH)
                    .where('f', MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.SOUTH)
                    .where('c', PlaceholderType.CONVEYOR, GAMetaBlocks.CONVEYOR_CASING.getState(ConveyorCasing.CasingType.values()[0]))
                    .where('r', PlaceholderType.ROBOT_ARM, GAMetaBlocks.ROBOT_ARM_CASING.getState(RobotArmCasing.CasingType.values()[0]))
                    .where('G', PlaceholderType.GLASS, GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.values()[0]))
                    .build();

    }

    @Override
    public float getDefaultZoom() {
        return 0.5F;
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.parallel_circuit_assembly_line.description").replace("§7", "§r"),
                        I18n.format("tj.multiblock.large_assembly_line.tooltip"),
                        I18n.format("tj.multiblock.parallel.extend.tooltip").replace("§7", "§r")},
                super.getDescription());
    }
}
