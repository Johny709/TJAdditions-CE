package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.metal.NuclearCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.util.EnumFacing;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class LargeNuclearReactorInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_NUCLEAR_REACTOR;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        final List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        final TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("~CEC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CCC~")
                .aisle("CCCCC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CCCCC")
                .aisle("CCCCC", "GNNNG", "GNNNG", "GNNNG", "GNNNG", "GNNNG", "GNNNG", "GNNNG", "CCCCC")
                .aisle("iCCCo", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CNNNC", "CCCCC")
                .aisle("~ISO~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CGC~", "~CMC~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.CLADDED_REACTOR_CASING))
                .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.OSMIRIDIUM_GLASS))
                .where('N', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.THORIUM))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST);
        final int maxTier = TJConfig.machines.disableLayersInJEI ? 4 : 15;
        for (int tier = TJConfig.machines.disableLayersInJEI ? 3 : 0; tier < maxTier; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.WEST)
                    .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.NORTH)
                    .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.SOUTH)
                    .build());
        }
        return shapeInfos;
    }
}
