package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.FieldGenCasing;
import gregicadditions.item.metal.NuclearCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.TJValues;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.integration.jei.multi.parallel.IParallelMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;

public class LargeNuclearReactorInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_NUCLEAR_REACTOR;
    }

    @Override
    public List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes) {
        final List<TJMultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        final int size = Math.min(TJConfig.machines.maxLayersInJEI, TJConfig.largeGasCentrifuge.maximumSlices);
        for (int shapeInfo = 1; shapeInfo <= size; shapeInfo++) {
            final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, RIGHT, DOWN)
                    .aisle("~~~~~C~~~~~", "~~~~GCG~~~~", "~~CCCCCCC~~", "~~CCCCCCC~~", "~GCCCCCCCG~", "CCCCCCCCCCC", "~GCCCCCCCG~", "~~CCCCCCC~~", "~~CCCCCCC~~", "~~~~GCG~~~~", "~~~~~C~~~~~");
            for (int layer = 0; layer < shapeInfo; layer++) {
                final String controllerS = layer == shapeInfo - 1 ? "~~~~~S~~~~~" : "~~~~~C~~~~~";
                final String hatchPlaces = layer == shapeInfo - 1 ? "~~Ii###Oo~~" : "~~CC###CC~~";
                final String maintenance = layer == shapeInfo - 1 ? "~~CM###EC~~" : "~~CC###CC~~";
                builder.aisle(controllerS, "~~~~GNG~~~~", hatchPlaces, "~~CR#A#mC~~", "~G#######G~", "CB#P#g#L#TC", "~G#######G~", "~~Ce#U#cC~~", maintenance, "~~~~GFG~~~~", "~~~~~C~~~~~");
            }
            builder.aisle("~~~~~C~~~~~", "~~~~GCG~~~~", "~~CCCCCCC~~", "~~CCCCCCC~~", "~GCCCCCCCG~", "CCCCCCCCCCC", "~GCCCCCCCG~", "~~CCCCCCC~~", "~~CCCCCCC~~", "~~~~GCG~~~~", "~~~~~C~~~~~");
            final TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            final int maxTier = TJConfig.machines.disableLayersInJEI ? 4 : 15;
            for (int tier = TJConfig.machines.disableLayersInJEI ? 3 : 0; tier < maxTier; tier++) {
                infos[tier] = builder.where('S', this.getController(), WEST)
                        .where('M', GATileEntities.MAINTENANCE_HATCH[0], EAST)
                        .where('E', this.getEnergyHatch(tier, false), EAST)
                        .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], WEST)
                        .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], WEST)
                        .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[tier], WEST)
                        .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[tier], WEST)
                        .where('C', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.CLADDED_REACTOR_CASING))
                        .where('N', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.NEPTUNIUM))
                        .where('R', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.CURIUM))
                        .where('A', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.AMERICIUM))
                        .where('m', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.MENDELEVIUM))
                        .where('B', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.BERKELIUM))
                        .where('P', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.PROTACTINIUM))
                        .where('L', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.PLUTONIUM))
                        .where('T', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.THORIUM))
                        .where('e', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.EINSTEINIUM))
                        .where('U', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.URANIUM))
                        .where('c', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.CALIFORNIUM))
                        .where('F', GAMetaBlocks.NUCLEAR_CASING.getState(NuclearCasing.CasingType.FERMIUM))
                        .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.values()[Math.min(6, tier)]))
                        .where('g', GAMetaBlocks.FIELD_GEN_CASING.getState(FieldGenCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        Arrays.stream(FieldGenCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.FIELD_GEN_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(GATransparentCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.TRANSPARENT_CASING.getItemVariant(casingType), COMPONENT_TIER_ANY_TOOLTIP));
    }

    @Override
    public float getDefaultZoom() {
        return 0.5F;
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.add(ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.parallel.extend.tooltip").replace("§7", "§r"),
                                I18n.format("tj.multiblock.processing_array.eut")},
                        super.getDescription()),
                I18n.format("tj.multiblock.universal.tooltip.2", TJValues.thousandFormat.format(TJConfig.industrialFusionReactor.maximumSlices)));
    }
}
