package tj.integration.jei.multi.steam;

import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMachineCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.machines.TJMetaTileEntities;

import java.util.List;

public class PrimitiveAlloyInfo extends MultiblockInfoPage {
    public PrimitiveAlloyInfo() {
    }

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.PRIMITIVE_ALLOY;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("IFF", "FFF", "XXX")
                .aisle("IFF", "S#F", "X#X")
                .aisle("OFW", "FFF", "XXX")
                .where('S', TJMetaTileEntities.PRIMITIVE_ALLOY, EnumFacing.WEST)
                .where('F', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS))
                .where('X', MetaBlocks.MACHINE_CASING.getState(BlockMachineCasing.MachineCasingType.BRONZE_HULL))
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.ULV], EnumFacing.WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.ULV], EnumFacing.WEST)
                .where('W', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.ULV], EnumFacing.EAST)
                .build();
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.default.description")};
    }
}
