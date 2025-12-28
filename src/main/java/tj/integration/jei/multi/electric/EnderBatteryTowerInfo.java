package tj.integration.jei.multi.electric;

import gregicadditions.item.CellCasing;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregicadditions.machines.GATileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import tj.TJConfig;
import tj.builder.multicontrollers.ExtendableMultiblockController;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.multi.parallel.IParallelMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class EnderBatteryTowerInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public ExtendableMultiblockController getController() {
        return TJMetaTileEntities.ENDER_BATTERY_TOWER;
    }

    @Override
    public List<MultiblockShapeInfo[]> getMatchingShapes(MultiblockShapeInfo[] shapes) {
        List<MultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        int size = Math.min(TJConfig.machines.maxLayersInJEI, this.getController().getMaxParallel());
        for (int shapeInfo = 1; shapeInfo <= size; shapeInfo++) {
            GAMultiblockShapeInfo.Builder builder = GAMultiblockShapeInfo.builder(FRONT, RIGHT, DOWN);
            builder.aisle("CCCCC", "CCCCC", "CCCCC", "CCCCC", "CCCCC");
            for (int layer = 0; layer < shapeInfo; layer++) {
                builder.aisle("GGGGG", "GcccG", "GcccG", "GcccG", "GGGGG");
            }
            builder.aisle("CCSCC", "CCCCC", "CCCCC", "CCCCC", "CEMeC");
            MultiblockShapeInfo[] infos = new MultiblockShapeInfo[15];
            for (int tier = 0; tier < infos.length; tier++) {
                infos[tier] = builder.where('S', this.getController(), EnumFacing.WEST)
                        .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.BOROSILICATE_GLASS))
                        .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.HASTELLOY_X78))
                        .where('c', GAMetaBlocks.CELL_CASING.getState(CellCasing.CellType.values()[Math.max(0, tier - 3)]))
                        .where('e', this.getEnergyHatch(tier, true), EnumFacing.EAST)
                        .where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                        .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.EAST)
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        Arrays.stream(GATransparentCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.TRANSPARENT_CASING.getItemVariant(casingType), COMPONENT_TIER_ANY_TOOLTIP));
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.HASTELLOY_X78), new TextComponentTranslation("gregtech.multiblock.preview.limit", 10)
                .setStyle(new Style().setColor(TextFormatting.RED)));
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gtadditions.multiblock.battery_tower.tooltip.1"),
                net.minecraft.client.resources.I18n.format("tj.multiblock.ender_battery_tower.description")};
    }
}
