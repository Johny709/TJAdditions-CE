package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockBoilerCasing;
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
import tj.machines.multi.parallel.MetaTileEntityParallelAlloyBlastSmelter;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelAlloyBlastSmelterInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelAlloyBlastSmelter getController() {
        return TJMetaTileEntities.PARALLEL_ALLOY_BLAST_FURNACE;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, RIGHT, DOWN);
        for (int layer = 0; layer < extent; layer++) {
            String mufflerMM = layer == 0 ? "CCCmCCC" : "CCCPCCC";
            builder.aisle("~CCCCC~", "CCCCCCC", "CCCCCCC", mufflerMM, "CCCCCCC", "CCCCCCC", "~CCCCC~");
            builder.aisle("~AAAAA~", "AcccccA", "Ac#c#cA", "AccPccA", "Ac#c#cA", "AcccccA", "~AAAAA~");
            builder.aisle("~AAAAA~", "AcccccA", "Ac#c#cA", "AccPccA", "Ac#c#cA", "AcccccA", "~AAAAA~");
        }
        return builder.aisle("~IiSoC~", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "~CCEMC~")
                .where('S', this.getController(), WEST)
                .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE))
                .where('A', GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.STABALLOY))
                .where('c', PlaceholderType.COIL)
                .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EAST)
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EAST)
                .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], WEST)
                .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], WEST)
                .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], WEST)
                .where('m', PlaceholderType.MUFFLER, GATileEntities.MUFFLER_HATCH[0], EnumFacing.UP)
                .build();


    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE), new TextComponentTranslation("gregtech.multiblock.preview.limit", 22)
                .setStyle(new Style().setColor(TextFormatting.RED)));
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_alloy_blast_smelter.description").replace("§7", "§r"),
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
