package tj.integration.jei.multi.steam;

import gregicadditions.jei.GAMultiblockShapeInfo;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.MetaBlocks;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.Collections;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.Charcoal;

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
                .aisle("~GGG~", "GCCCG", "GCCCG", "GCCCG", "~GGG~")
                .aisle("~GGG~", "GCCCG", "GCCCG", "GCCCG", "~GGG~")
                .aisle("~~~~~", "~GGG~", "~GSG~", "~GGG~", "~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', MetaBlocks.COMPRESSED.get(Charcoal).getDefaultState())
                .where('G', Blocks.DIRT.getDefaultState())
                .build());
    }

    @Override
    public MultiblockControllerBase getController() {
        return this.advanced ? TJMetaTileEntities.CHARCOAL_PIT_ADVANCED : TJMetaTileEntities.CHARCOAL_PIT;
    }
}
