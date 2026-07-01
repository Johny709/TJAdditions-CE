package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
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
    public MultiblockShapeInfo getMatchingShapes(int extent) {
       return TJMultiblockShapeInfo.builder(FRONT, RIGHT, DOWN)
                .aisle("oCC", "CCC", "CCC")
                .aisle("oCC", "CPC", "CCC")
                .aisle("oCC", "CPC", "CCC")
                .aisle("oCC", "CPC", "CCC")
                .aisle("oMC", "CPC", "CCC")
                .aisle("ISO", "CCC", "CEC")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.BABBITT_ALLOY))
                .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE))
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                .where('I', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.WEST)
                .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], EnumFacing.WEST)
                .build();
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
