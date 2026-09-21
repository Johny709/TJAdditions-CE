package tj.integration.jei.multi.steam;

import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMachineCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.util.EnumFacing;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class PrimitiveAlloyInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.PRIMITIVE_ALLOY;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
       return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("CCC", "CCC", "BBB")
                .aisle("CCC", "C#C", "B#B")
                .aisle("IOI", "iSC", "BBB")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS))
                .where('B', MetaBlocks.MACHINE_CASING.getState(BlockMachineCasing.MachineCasingType.BRONZE_HULL))
                .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                .where('O', PlaceholderType.OUTPUT_BUS,MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.WEST)
                .build();
    }
}
