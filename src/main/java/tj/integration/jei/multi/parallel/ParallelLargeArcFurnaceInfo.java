package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.*;
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


public class ParallelLargeArcFurnaceInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public ParallelRecipeMapMultiblockController getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_ARC_FURNACE;
    }

    @Override
    public List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes) {
        List<TJMultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        int size = Math.min(TJConfig.machines.maxLayersInJEI, this.getController().getMaxParallel());
        for (int shapeInfo = 1; shapeInfo <= size; shapeInfo++) {
            TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, RIGHT, DOWN);
            for (int layer = 0; layer < shapeInfo; layer++) {

                String entityS = layer == shapeInfo - 1 ? "~GSG~" : "~GGG~";

                builder.aisle("~CCC~", "CCcCC", "CCcCC", "CCcCC", "~CCC~");
                builder.aisle(entityS, "GT#TG", "GP#PG", "GT#TG", "~GGG~");
            }
            builder.aisle("~IMO~", "CCcCC", "CCcCC", "CCcCC", "~iEo~");
            TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            for (int tier = 0; tier < infos.length; tier++) {
                infos[tier] = builder.where('S', this.getController(), WEST)
                        .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF))
                        .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                        .where('P', GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .where('c', MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.CUPRONICKEL))
                        .where('T', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE))
                        .where('M', GATileEntities.MAINTENANCE_HATCH[0], WEST)
                        .where('E', this.getEnergyHatch(tier, false), EAST)
                        .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], WEST)
                        .where('i', MetaTileEntities.ITEM_EXPORT_BUS[tier], EAST)
                        .where('O', MetaTileEntities.FLUID_IMPORT_HATCH[tier], WEST)
                        .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EAST)
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
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
