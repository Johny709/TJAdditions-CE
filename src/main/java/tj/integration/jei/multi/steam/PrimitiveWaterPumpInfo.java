package tj.integration.jei.multi.steam;

import gregicadditions.jei.GAMultiblockShapeInfo;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.util.EnumFacing;
import tj.blocks.AbilityBlocks;
import tj.blocks.TJMetaBlocks;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.Wood;

public class PrimitiveWaterPumpInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.PRIMITIVE_WATER_PUMP;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            shapeInfos.add(GAMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                    .aisle(i == 1 ? new String[]{"CCCCC", "~~F~~", "~~F~~"} : new String[]{"CCCC", "~~F~", "~~F~"})
                    .aisle(i == 1 ? new String[]{"CCOCC", "F~~~F", "FFFFF"} : new String[]{"CCOC", "F~~F", "FFFF"})
                    .aisle(i == 1 ? new String[]{"SCCCC", "~~F~~", "~~F~~"} : new String[]{"SCCC", "~~F~", "~~F~"})
                    .where('S', this.getController(), EnumFacing.WEST)
                    .where('C', TJMetaBlocks.ABILITY_BLOCKS.getState(AbilityBlocks.AbilityType.PRIMITIVE_PUMP_CASING))
                    .where('F', MetaBlocks.FRAMES.get(Wood).getDefaultState())
                    .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[1], EnumFacing.UP)
                    .build());
        }
        return shapeInfos;
    }
}
