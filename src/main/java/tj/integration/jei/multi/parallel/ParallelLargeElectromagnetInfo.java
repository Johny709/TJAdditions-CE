package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.FieldGenCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelLargeElectromagnetInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public ParallelRecipeMapMultiblockController getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_ELECTROMAGNET;
    }

    @Override
    public List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes) {
        List<TJMultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        int size = Math.min(TJConfig.machines.maxLayersInJEI, this.getController().getMaxParallel());
        for (int shapeInfo = 1; shapeInfo <= size; shapeInfo++) {
            TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, UP, LEFT);
            builder.aisle("~~~~~", "~CCC~", "~CEC~", "~CCC~", "~~~~~");
            for (int layer = 0; layer < shapeInfo; layer++) {
                builder.aisle("~C~C~", "C#C#C", "G###G", "C#C#C", "~C~C~");
                builder.aisle("~C~C~", "C#C#C", "GF#FG", "C#C#C", "~C~C~");
            }
            builder.aisle("~C~C~", "C#C#C", "G###G", "C#C#C", "~C~C~")
                    .aisle("~~~~~", "~CCC~", "~ISO~", "~CMC~", "~~~~~");
            TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            for (int tier = 0; tier < infos.length; tier++) {
                infos[tier] = builder.where('S', this.getController(), WEST)
                        .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.BABBITT_ALLOY))
                        .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.IRIDIUM_GLASS))
                        .where('F', GAMetaBlocks.FIELD_GEN_CASING.getState(FieldGenCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .where('M', GATileEntities.MAINTENANCE_HATCH[0], WEST)
                        .where('E', this.getEnergyHatch(tier, false), EAST)
                        .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], WEST)
                        .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], WEST)
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.BABBITT_ALLOY), new TextComponentTranslation("gregtech.multiblock.preview.limit", 10)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.FIELD_GEN_CASING.getItemVariant(FieldGenCasing.CasingType.FIELD_GENERATOR_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_large_electromagnet.description"),
                I18n.format("tj.multiblock.parallel.description"),
                I18n.format("tj.multiblock.parallel.extend.tooltip")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
