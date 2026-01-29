package tj.integration.jei.multi.parallel;

import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
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


public class ParallelElectricBlastFurnaceInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public ParallelRecipeMapMultiblockController getController() {
        return TJMetaTileEntities.PARALLEL_ELECTRIC_BLAST_FURNACE;
    }

    @Override
    public List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes) {
        List<TJMultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        int size = Math.min(TJConfig.machines.maxLayersInJEI, this.getController().getMaxParallel());
        for (int shapeInfo = 1; shapeInfo <= size; shapeInfo++) {
            TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, RIGHT, DOWN);
            for (int layer = 0; layer < shapeInfo; layer++) {
                String muffler = layer == 0 ? "CCmCC" : "CCPCC";
                builder.aisle("CCCCC", "CCCCC", muffler, "CCCCC", "CCCCC");
                builder.aisle("ccccc", "c#c#c", "ccPcc", "c#c#c", "ccccc");
                builder.aisle("ccccc", "c#c#c", "ccPcc", "c#c#c", "ccccc");
            }
            builder.aisle("IiSOo", "CCCCC", "CCCCC", "CCCCC", "CCEMC");
            TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            for (int tier = 0; tier < infos.length; tier++) {
                infos[tier] = builder.where('S', this.getController(), WEST)
                        .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF))
                        .where('c', this.getCoils(tier))
                        .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                        .where('M', GATileEntities.MAINTENANCE_HATCH[0], EAST)
                        .where('E', this.getEnergyHatch(tier, false), EAST)
                        .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], WEST)
                        .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[tier], WEST)
                        .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], WEST)
                        .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[tier], WEST)
                        .where('m', GATileEntities.MUFFLER_HATCH[0], EnumFacing.UP)
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
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
                I18n.format("tj.multiblock.parallel_electric_blast_furnace.description"),
                I18n.format("tj.multiblock.parallel.description"),
                I18n.format("tj.multiblock.parallel.extend.tooltip"),
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
