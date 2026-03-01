package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.ConveyorCasing;
import gregicadditions.item.components.RobotArmCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.util.EnumFacing;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.integration.jei.multi.parallel.IParallelMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class LargeAssemblyLineInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_ASSEMBLY_LINE;
    }

    @Override
    public List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes) {
        List<TJMultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        for (int layer = 1; layer < 5; layer++) {
            TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                    .aisle("FOICf", "CCCCC", "C###C", "CCCCC", "CCCCC", "~CCC~", "~~e~~");
            for (int i = 0; i < 4 * layer; i++) {
                builder.aisle("FCICf", "G#c#G", "G###G", "G#r#G", "EAaAE", "~EAE~", "~~C~~");
            }
            builder.aisle("FCICf", "CCCCC", "C###C", "CCCCC", "CCSCC", "~CMC~", "~~C~~")
                    .where('S', this.getController(), EnumFacing.WEST)
                    .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                    .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                    .where('E', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                    .where('A', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.ASSEMBLY_LINE_CASING))
                    .where('a', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLER_CASING));
            TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            for (int tier = 0; tier < 15; tier++) {
                infos[tier] = builder.where('e', this.getEnergyHatch(tier, false), EnumFacing.WEST)
                        .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.DOWN)
                        .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.EAST)
                        .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.NORTH)
                        .where('f', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.SOUTH)
                        .where('c', GAMetaBlocks.CONVEYOR_CASING.getState(ConveyorCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .where('r', GAMetaBlocks.ROBOT_ARM_CASING.getState(RobotArmCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.values()[Math.min(6, tier)]))
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
    }

    @Override
    public float getDefaultZoom() {
        return 0.5F;
    }
}
