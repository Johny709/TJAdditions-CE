package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.*;
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
import tj.machines.multi.parallel.MetaTileEntityParallelLargeArcFurnace;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelLargeArcFurnaceInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelLargeArcFurnace getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_ARC_FURNACE;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, RIGHT, DOWN);
        for (int layer = 0; layer < extent; layer++) {
            String entityS = layer == extent - 1 ? "~GSG~" : "~GGG~";
            builder.aisle("~CCC~", "CCcCC", "CCcCC", "CCcCC", "~CCC~");
            builder.aisle(entityS, "GT#TG", "GP#PG", "GT#TG", "~GGG~");
        }
        return builder.aisle("~IMO~", "CCcCC", "CCcCC", "CCcCC", "~iEo~").where('S', this.getController(), WEST)
                    .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF))
                    .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                    .where('P', PlaceholderType.PUMP , GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[0]))
                    .where('c', MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.CUPRONICKEL))
                    .where('T', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE))
                    .where('M', GATileEntities.MAINTENANCE_HATCH[0], WEST)
                    .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EAST)
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], WEST)
                    .where('i', PlaceholderType.OUTPUT_BUS,MetaTileEntities.ITEM_EXPORT_BUS[0], EAST)
                    .where('O', PlaceholderType.INPUT_HATCH,MetaTileEntities.FLUID_IMPORT_HATCH[0], WEST)
                    .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], EAST)
                    .build();

    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF), new TextComponentTranslation("gregtech.multiblock.preview.limit", 9)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.PUMP_CASING.getItemVariant(PumpCasing.CasingType.PUMP_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_large_arc_furnace.description"),
                I18n.format("tj.multiblock.parallel.description"),
                I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.2").replace("§7", "§0").replace("§r", "§r§0")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
