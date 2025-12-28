package tj.integration.jei.multi.steam;

import com.google.common.collect.Lists;
import gregicadditions.item.metal.MetalCasing1;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.machines.TJMetaTileEntities;

import java.util.List;

import static gregicadditions.item.GAMetaBlocks.METAL_CASING_1;

public class HeatExchangerInfo extends MultiblockInfoPage {
    public  HeatExchangerInfo() {
    }

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.HEAT_EXCHANGER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("IIF", "FFF", "FFF")
                .aisle("WFF", "S#F", "FFF")
                .aisle("WFF", "FFF", "FFF")
                .where('S', TJMetaTileEntities.HEAT_EXCHANGER, EnumFacing.WEST)
                .where('F', METAL_CASING_1.getState(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE))
                .where('W', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.IV], EnumFacing.WEST)
                .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.IV], EnumFacing.NORTH)
                .build();
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.heat_exchanger.description")};
    }
}
