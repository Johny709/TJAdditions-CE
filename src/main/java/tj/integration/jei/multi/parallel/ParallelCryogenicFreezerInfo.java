package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockBoilerCasing;
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
import tj.machines.multi.parallel.MetaTileEntityParallelCryogenicFreezer;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelCryogenicFreezerInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelCryogenicFreezer getController() {
        return TJMetaTileEntities.PARALLEL_CRYOGENIC_FREEZER;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {

        final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, RIGHT, DOWN);
        for (int layer = 0; layer < extent; layer++) {
            String entityS = layer == extent - 1 ? "~ISO~" : "~CCC~";
            String energyH = layer == extent - 1 ? "~CEM~" : "~CCC~";
            builder.aisle("~CCC~", "CCCCC", "CCCCC", "CCCCC", "~CCC~");
            builder.aisle(entityS, "C#P#C", "CPPPC", "C#P#C", energyH);
        }
        return builder.aisle("~iCo~", "CCCCC", "CCCCC", "CCCCC", "~CCC~")
                    .where('S', this.getController(), WEST)
                    .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.INCOLOY_MA956))
                    .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                    .where('M', GATileEntities.MAINTENANCE_HATCH[0], EAST)
                    .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EAST)
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], WEST)
                    .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[0], WEST)
                    .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], WEST)
                    .build();
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.INCOLOY_MA956), new TextComponentTranslation("gregtech.multiblock.preview.limit", 16)
                .setStyle(new Style().setColor(TextFormatting.RED)));
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_cryogenic_freezer.description").replace("§7", "§r"),
                I18n.format("tj.multiblock.parallel.description"),
                I18n.format("gregtech.multiblock.vol_cryo.description")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
