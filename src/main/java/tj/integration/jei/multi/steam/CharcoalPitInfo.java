package tj.integration.jei.multi.steam;

import gregicadditions.jei.GAMultiblockShapeInfo;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.Collections;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class CharcoalPitInfo extends TJMultiblockInfoPage {

    private final boolean advanced;

    public CharcoalPitInfo(boolean advanced) {
        this.advanced = advanced;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Collections.singletonList(GAMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("~~~~~", "~GGG~", "~GGG~", "~GGG~", "~~~~~")
                .aisle("~GGG~", "GCCCG", "GCCCG", "GCCCG", "~GGG~")
                .aisle("~GGG~", "GCCCG", "GCCCG", "GCCCG", "~GSG~")
                .aisle("~GGG~", "GCCCG", "GCCCG", "GCCCG", "~GGG~")
                .aisle("~~~~~", "~GGG~", "~GGG~", "~GGG~", "~~~~~")
                .where('S', this.getController(), EnumFacing.UP)
                .where('G', Blocks.DIRT.getDefaultState())
                .where('C', Blocks.LOG.getDefaultState())
                .build());
    }

    @Override
    public MultiblockControllerBase getController() {
        return this.advanced ? TJMetaTileEntities.CHARCOAL_PIT_ADVANCED : TJMetaTileEntities.CHARCOAL_PIT;
    }
}
