package tj.integration.jei.multi.parallel;

import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;
import tj.machines.multi.parallel.MetaTileEntityParallelElectricBlastFurnace;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelElectricBlastFurnaceInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelElectricBlastFurnace getController() {
        return TJMetaTileEntities.PARALLEL_ELECTRIC_BLAST_FURNACE;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, RIGHT, DOWN);
        for (int layer = 0; layer < extent; layer++) {
            String muffler = layer == 0 ? "CCmCC" : "CCPCC";
            builder.aisle("CCCCC", "CCCCC", muffler, "CCCCC", "CCCCC");
            builder.aisle("ccccc", "c#c#c", "ccPcc", "c#c#c", "ccccc");
            builder.aisle("ccccc", "c#c#c", "ccPcc", "c#c#c", "ccccc");
        }
        return builder.aisle("IiSOo", "CCCCC", "CCCCC", "CCCCC", "CCEMC").where('S', this.getController(), WEST)
                    .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF))
                    .where('c', PlaceholderType.COIL)
                    .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                    .where('M', GATileEntities.MAINTENANCE_HATCH[0], EAST)
                    .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EAST)
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], WEST)
                    .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], WEST)
                    .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], WEST)
                    .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], WEST)
                    .where('m', GATileEntities.MUFFLER_HATCH[0], EnumFacing.UP)
                    .build();

    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF), new TextComponentTranslation("gregtech.multiblock.preview.limit", 12)
                .setStyle(new Style().setColor(TextFormatting.RED)));
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_electric_blast_furnace.description").replace("§7", "§r"),
                I18n.format("tj.multiblock.parallel.description"),
                I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.1").replace("§7", "§0").replace("§r", "§r§0"),
                I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.2").replace("§7", "§0").replace("§r", "§r§0"),
                I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.3").replace("§7", "§0").replace("§r", "§r§0")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
