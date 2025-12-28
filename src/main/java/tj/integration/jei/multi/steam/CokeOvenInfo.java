package tj.integration.jei.multi.steam;

import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.machines.TJMetaTileEntities;

import java.util.List;

public class CokeOvenInfo extends MultiblockInfoPage {
    public  CokeOvenInfo() {
    }
    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.COKE_OVEN;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("IFF", "FFF", "FFF")
                .aisle("OFF", "S#F", "FFF")
                .aisle("WFF", "FFF", "FFF")
                .where('S', TJMetaTileEntities.COKE_OVEN, EnumFacing.WEST)
                .where('F', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.COKE_BRICKS))
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.ULV], EnumFacing.WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.ULV], EnumFacing.WEST)
                .where('W', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.ULV], EnumFacing.WEST)
                .build();
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.default.description")};
    }
}
