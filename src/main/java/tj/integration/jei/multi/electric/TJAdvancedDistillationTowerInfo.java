package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockBoilerCasing;
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

public class TJAdvancedDistillationTowerInfo extends TJMultiblockInfoPage {
    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.ADVANCED_DISTILLATION_TOWER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        final List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        final TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, RIGHT, DOWN)
                .aisle("oCC", "CCC", "CCC")
                .aisle("oCC", "CPC", "CCC")
                .aisle("oCC", "CPC", "CCC")
                .aisle("oCC", "CPC", "CCC")
                .aisle("oMC", "CPC", "CCC")
                .aisle("ISO", "CCC", "CEC")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.BABBITT_ALLOY))
                .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE));
        final int maxTier = TJConfig.machines.disableLayersInJEI ? 4 : 15;
        for (int tier = TJConfig.machines.disableLayersInJEI ? 3 : 0; tier < maxTier; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.WEST)
                    .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.WEST)
                    .build());
        }
        return shapeInfos;
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.temporary"),
                        I18n.format("tj.multiblock.distillation_tower.layers", 2, 13).replace("§7", "§r"),
                        I18n.format("gregtech.multiblock.advanced_distillation_tower.description1"),
                        I18n.format("gregtech.multiblock.advanced_distillation_tower.description2"),
                        I18n.format("gregtech.multiblock.advanced_distillation_tower.description3"),
                        I18n.format("gregtech.multiblock.advanced_distillation_tower.description4")},
                super.getDescription());
    }
}
