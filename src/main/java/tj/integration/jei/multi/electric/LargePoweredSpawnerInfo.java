package tj.integration.jei.multi.electric;

import com.google.common.collect.Lists;
import gregicadditions.machines.GATileEntities;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.machines.TJMetaTileEntities;

import java.util.List;

public class LargePoweredSpawnerInfo extends MultiblockInfoPage {

    public LargePoweredSpawnerInfo() {
    }

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_POWERED_SPAWNER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("ICCCC", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .aisle("LCCCC", "~TMT~", "~TMT~", "~TMT~", "CGGGC")
                .aisle("SCCCC", "~MEM~", "~MEM~", "~MEM~", "HGGGC")
                .aisle("OCCCC", "~TMT~", "~TMT~", "~TMT~", "CGGGC")
                .aisle("JCCCC", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .where('S', TJMetaTileEntities.LARGE_POWERED_SPAWNER, EnumFacing.WEST)
                .where('C', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.SOUL_CASING))
                .where('F', new BlockInfo(Block.getBlockFromName("gregtech:frame_protactinium")))
                .where('T', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                .where('M', new BlockInfo(Block.getBlockFromName("enderio:block_decoration1")))
                .where('E', Block.getBlockFromName("enderio:block_alloy").getStateFromMeta(8))
                .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.MV], EnumFacing.WEST)
                .where('L', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.MV], EnumFacing.WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.MV], EnumFacing.WEST)
                .where('J', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LuV], EnumFacing.WEST)
                .where('H', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .build();
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.large_powered_spawner.description")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
