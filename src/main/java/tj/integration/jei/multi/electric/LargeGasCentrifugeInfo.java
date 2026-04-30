package tj.integration.jei.multi.electric;

import gregicadditions.GAMaterials;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
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
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;

public class LargeGasCentrifugeInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_GAS_CENTRIFUGE;
    }

    @Override
    public List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes) {
        final List<TJMultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        final int size = Math.min(TJConfig.machines.maxLayersInJEI, TJConfig.largeGasCentrifuge.maximumSlices);
        for (int shapeInfo = 1; shapeInfo <= size; shapeInfo++) {
            final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, UP, LEFT)
                    .aisle("~CEC~", "~CCC~", "~N~N~", "~N~N~", "~N~N~", "~N~N~", "~N~N~");
            for (int layer = 1; layer < shapeInfo; layer++) {
                builder.aisle("CCCCC", "CCCCC", "NPoPN", "NPNPN", "NPNPN", "NPNPN", "NPNPN");
                builder.aisle("CCCCC", "CCCCC", "~o~o~", "~N~N~", "~N~N~", "~N~N~", "~N~N~");
            }
            builder.aisle("CCCCC", "CCCCC", "NPoPN", "NPNPN", "NPNPN", "NPNPN", "NPNPN")
                    .aisle("~iMC~", "~ISO~", "~N~N~", "~N~N~", "~N~N~", "~N~N~", "~N~N~");
            final TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            final int maxTier = TJConfig.machines.disableLayersInJEI ? 4 : 15;
            for (int tier = TJConfig.machines.disableLayersInJEI ? 3 : 0; tier < maxTier; tier++) {
                infos[tier] = builder.where('S', this.getController(), WEST)
                        .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN))
                        .where('o', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE))
                        .where('N', MetaBlocks.FRAMES.get(GAMaterials.Mendelevium.getMaterial()).getDefaultState())
                        .where('M', GATileEntities.MAINTENANCE_HATCH[0], WEST)
                        .where('E', this.getEnergyHatch(tier, false), EAST)
                        .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[tier], WEST)
                        .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[tier], WEST)
                        .where('i', MetaTileEntities.ITEM_IMPORT_BUS[tier], WEST)
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.add(ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.parallel.extend.tooltip").replace("§7", "§r"),
                I18n.format("tj.multiblock.processing_array.eut")},
                super.getDescription()),
                I18n.format("tj.multiblock.universal.tooltip.2", TJValues.thousandFormat.format(TJConfig.industrialFusionReactor.maximumSlices)));
    }
}
