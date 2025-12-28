package tj.integration.jei.multi.electric;

import com.google.common.collect.Lists;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
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

public class LargeVialProcessorInfo extends MultiblockInfoPage {

    public LargeVialProcessorInfo() {
    }

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_VIAL_PROCESSOR;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("ICCCC", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .aisle("LTTTC", "~BGB~", "~BGB~", "~BGB~", "CTTTC")
                .aisle("STETC", "~GEG~", "~GEG~", "~GEG~", "HTETC")
                .aisle("OTTTC", "~BGB~", "~BGB~", "~BGB~", "CTTTC")
                .aisle("JCCCC", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .where('S', TJMetaTileEntities.LARGE_VIAL_PROCESSOR, EnumFacing.WEST)
                .where('C', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.SOUL_CASING))
                .where('B', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TUNGSTENSTEEL_GEARBOX_CASING))
                .where('T', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                .where('F', new BlockInfo(Block.getBlockFromName("gregtech:frame_protactinium")))
                .where('E', Block.getBlockFromName("enderio:block_alloy").getStateFromMeta(8))
                .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                .where('I', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.MV], EnumFacing.WEST)
                .where('L', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.MV], EnumFacing.WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.EV], EnumFacing.WEST)
                .where('J', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LuV], EnumFacing.WEST)
                .where('H', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .build();
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.large_vial_processor.description")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
