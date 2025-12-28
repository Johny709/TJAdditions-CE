package tj.integration.jei.multi.electric;

import com.google.common.collect.Lists;
import gregicadditions.machines.GATileEntities;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.machines.TJMetaTileEntities;

import java.util.List;

public class DragonReplicatorInfo extends MultiblockInfoPage {

    public DragonReplicatorInfo() {
    }

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.DRAGON_REPLICATOR;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("OEF", "FFF", "FFF")
                .aisle("OFF", "SDF", "MFF")
                .aisle("IFF", "FFF", "FFF")
                .where('S', TJMetaTileEntities.DRAGON_REPLICATOR, EnumFacing.WEST)
                .where('F', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.AWAKENED_CASING))
                .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.IV], EnumFacing.WEST)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.ULV], EnumFacing.WEST)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.UV], EnumFacing.NORTH)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('D', Blocks.DRAGON_EGG.getDefaultState())
                .build();
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.dragon_egg_replicator.description")};
    }
}
