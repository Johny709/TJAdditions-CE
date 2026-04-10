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
    public List<MultiblockShapeInfo> getMatchingShapes() {
        final List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, UP, LEFT)
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
                .where('a', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLER_CASING));
        final int maxTier = TJConfig.machines.disableLayersInJEI ? 4 : 15;
        for (int tier = TJConfig.machines.disableLayersInJEI ? 3 : 0; tier < maxTier; tier++) {
            shapeInfos.add(builder.where('e', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('A', this.getVoltageCasing(tier))
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.DOWN)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.EAST)
                    .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.NORTH)
                    .where('f', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.SOUTH)
                    .where('c', GAMetaBlocks.CONVEYOR_CASING.getState(ConveyorCasing.CasingType.values()[Math.max(0, tier - 1)]))
                    .where('r', GAMetaBlocks.ROBOT_ARM_CASING.getState(RobotArmCasing.CasingType.values()[Math.max(0, tier - 1)]))
                    .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.values()[Math.min(6, tier)]))
                    .build());
        }
        return shapeInfos;
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
