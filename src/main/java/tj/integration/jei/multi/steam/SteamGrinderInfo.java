package tj.integration.jei.multi.steam;

import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.util.EnumFacing;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;


import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class SteamGrinderInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.STEAM_GRINDER;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("CCC", "CCC", "CCC")
                .aisle("CCC", "C#C", "CCC")
                .aisle("CHC", "ISO", "CCC")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('I', GATileEntities.STEAM_INPUT_BUS, EnumFacing.WEST)
                .where('O', GATileEntities.STEAM_OUTPUT_BUS, EnumFacing.WEST)
                .where('H', GATileEntities.STEAM_HATCH, EnumFacing.WEST)
                .build();
    }
}
