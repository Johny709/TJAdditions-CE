package tj.integration.jei.multi.steam;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
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
        for (int i = 0; i < 4; i++) {
            shapeInfos.add(GAMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                    .aisle(i % 2 == 0 ? new String[]{"CCCC", "~~F~", "~~F~"} : new String[]{"CCCCC", "~~F~~", "~~F~~"})
                    .aisle(i % 2 == 0 ? new String[]{"CPOC", "F~~F", "FFFF"} : new String[]{"CPOPC", "F~~~F", "FFFFF"})
                    .aisle(i % 2 == 0 ? new String[]{"SCCC", "~~F~", "~~F~"} : new String[]{"SCCCC", "~~F~~", "~~F~~"})
                    .where('S', this.getController(), EnumFacing.WEST)
                    .where('C', TJMetaBlocks.ABILITY_BLOCKS.getState(AbilityBlocks.AbilityType.PRIMITIVE_PUMP_CASING))
                    .where('P', i < 2 ? TJMetaBlocks.ABILITY_BLOCKS.getState(AbilityBlocks.AbilityType.PRIMITIVE_PUMP_CASING) : GAMetaBlocks.PUMP_CASING.getDefaultState())
                    .where('F', MetaBlocks.FRAMES.get(Wood).getDefaultState())
                    .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[1], EnumFacing.UP)
                    .build());
        }
        return shapeInfos;
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("tj.multiblock.primitive_water_pump.description", 500, 1280).replace("§7", "§r")};
    }
}
