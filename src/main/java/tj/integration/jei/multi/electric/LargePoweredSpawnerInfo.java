package tj.integration.jei.multi.electric;

import com.google.common.collect.Lists;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

public class LargePoweredSpawnerInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_POWERED_SPAWNER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("ICCCC", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .aisle("LCCCC", "~TMT~", "~TMT~", "~TMT~", "CGGGC")
                .aisle("SCCCC", "~MEM~", "~MEM~", "~MEM~", "HGGGC")
                .aisle("OCCCC", "~TMT~", "~TMT~", "~TMT~", "CGGGC")
                .aisle("JCCCC", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.SOUL_CASING))
                .where('F', new BlockInfo(Block.getBlockFromName("gregtech:frame_protactinium")))
                .where('T', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                .where('M', new BlockInfo(Block.getBlockFromName("enderio:block_decoration1")))
                .where('E', Block.getBlockFromName("enderio:block_alloy").getStateFromMeta(8))
                .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                .where('H', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST);
        for (int tier = 0; tier < 15; tier++) {
            shapeInfos.add(builder.where('J', this.getEnergyHatch(tier, false), EnumFacing.WEST)
                    .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[Math.min(9, tier)], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[Math.min(9, tier)], EnumFacing.WEST)
                    .where('L', MetaTileEntities.ITEM_IMPORT_BUS[Math.min(9, tier)], EnumFacing.WEST)
                    .build());
        }
        return shapeInfos;
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
