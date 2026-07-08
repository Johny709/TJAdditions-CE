package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;
import tj.machines.multi.parallel.MetaTileEntityParallelLargeCanningMachine;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelLargeCanningMachineInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelLargeCanningMachine getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_CANNING_MACHINE;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {

        final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, UP, LEFT);
        builder.aisle("~~P~~", "~MPE~", "PPPPP", "~CPC~", "~~P~~");
        for (int layer = 0; layer < extent; layer++) {
            builder.aisle("~~P~~", "~G#G~", "P#p#P", "~G#G~", "~~P~~");
        }
        return builder.aisle("~~P~~", "~iPo~", "PPSPP", "~IPO~", "~~P~~").where('S', this.getController(), WEST)
                    .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                    .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.BOROSILICATE_GLASS))
                    .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE))
                    .where('p', PlaceholderType.PUMP,GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[0]))
                    .where('M', GATileEntities.MAINTENANCE_HATCH[0], EAST)
                    .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EAST)
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], WEST)
                    .where('i', MetaTileEntities.ITEM_EXPORT_BUS[0], WEST)
                    .where('O', MetaTileEntities.FLUID_IMPORT_HATCH[0], WEST)
                    .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], WEST)
                    .build();

    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.STEEL_SOLID), new TextComponentTranslation("gregtech.multiblock.preview.limit", 2)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.PUMP_CASING.getItemVariant(PumpCasing.CasingType.PUMP_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_large_canning_machine.description"),
                I18n.format("tj.multiblock.parallel.description")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
