package tj.integration.jei.multi.electric;

import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class TJMegaDistillationTowerInfo extends TJMultiblockInfoPage {
    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.MEGA_DISTILLATION_TOWER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        final List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        final TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, RIGHT, DOWN)
                .aisle("~CCC~", "CCCCC", "CCCCC", "CCCCC", "~CCC~")
                .aisle("~oCC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~oCC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~oCC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~oCC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~oCC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~oCC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~oCC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~oCC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~oCC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~oCC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~oCC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~oMC~", "CcPcC", "CPFPC", "CcPcC", "~CCC~")
                .aisle("~ISO~", "CCCCC", "CCCCC", "CCCCC", "~CEC~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN))
                .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                .where('c', MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.NICHROME))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST);
        final int maxTier = TJConfig.machines.disableLayersInJEI ? 4 : 15;
        for (int tier = TJConfig.machines.disableLayersInJEI ? 3 : 0; tier < maxTier; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.WEST)
                    .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.WEST)
                    .where('F', this.getVoltageCasing(tier))
                    .build());
        }
        return shapeInfos;
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.temporary"),
                        I18n.format("tj.multiblock.mega").replace("§7", "§r"),
                        I18n.format("tj.multiblock.distillation_tower.layers", 3, 14).replace("§7", "§r")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5F;
    }
}
