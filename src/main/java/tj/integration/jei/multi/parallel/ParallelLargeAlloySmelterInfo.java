package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAHeatingCoil;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;
import tj.machines.multi.parallel.MetaTileEntityParallelLargeAlloySmelter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelLargeAlloySmelterInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelLargeAlloySmelter getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_ALLOY_SMELTER;
    }

    @Override
    public List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes) {
        final List<TJMultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        final int size = Math.min(TJConfig.machines.maxLayersInJEI, this.getController().getMaxParallel());
        for (int shapeInfo = 1; shapeInfo <= size; shapeInfo++) {
            final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, UP, LEFT);
            builder.aisle("~C~C~", "CCCCC", "~CEC~", "CCCCC", "~C~C~");
            for (int layer = 0; layer < shapeInfo; layer++) {
                builder.aisle("~c~c~", "c#c#c", "~c#c~", "c#c#c", "~c~c~");
                builder.aisle("~c~c~", "c#c#c", "~c#c~", "c#c#c", "~c~c~");
            }
            builder.aisle("~c~c~", "c#c#c", "~c#c~", "c#c#c", "~c~c~")
                    .aisle("~C~C~", "CCCCC", "~ISO~", "CCMCC", "~C~C~");
            final TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            final int maxTier = TJConfig.machines.disableLayersInJEI ? 4 : 15;
            for (int tier = TJConfig.machines.disableLayersInJEI ? 3 : 0; tier < maxTier; tier++) {
                infos[tier] = builder.where('S', this.getController(), WEST)
                        .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE))
                        .where('M', GATileEntities.MAINTENANCE_HATCH[0], WEST)
                        .where('c', this.getCoils(tier))
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
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE), new TextComponentTranslation("gregtech.multiblock.preview.limit", 7)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        Arrays.stream(BlockWireCoil.CoilType.values()).forEach(coilType -> this.addBlockTooltip(MetaBlocks.WIRE_COIL.getItemVariant(coilType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(GAHeatingCoil.CoilType.values()).forEach(coilType -> this.addBlockTooltip(GAMetaBlocks.HEATING_COIL.getItemVariant(coilType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_large_alloy_smelter.description"),
                I18n.format("tj.multiblock.parallel.description")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
