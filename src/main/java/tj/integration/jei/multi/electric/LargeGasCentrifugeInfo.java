package tj.integration.jei.multi.electric;

import gregicadditions.GAMaterials;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
                    .aisle("~CEC~", "~CCC~", "~T~T~", "~p~p~", "~U~U~", "~N~N~", "~l~l~", "~A~A~", "~R~R~", "~B~B~", "~c~c~", "~e~e~", "~F~F~", "~m~m~");
            for (int layer = 1; layer < shapeInfo; layer++) {
                builder.aisle("CCCCC", "CCCCC", "pPoPp", "UPUPU", "NPNPN", "NPNPN", "lPlPl", "APAPA", "RPRPR", "BPBPB", "cPcPc", "ePePe", "FPFPF", "mPmPm");
                builder.aisle("CCCCC", "CCCCC", "~o~o~", "~U~U~", "~N~N~", "~N~N~", "~l~l~", "~A~A~", "~R~R~", "~B~B~", "~c~c~", "~e~e~", "~F~F~", "~m~m~");
            }
            builder.aisle("CCCCC", "CCCCC", "NPoPN", "NPNPN", "UPNPU", "NPNPN", "lPlPl", "APAPA", "RPRPR", "BPBPB", "cPcPc", "ePePe", "FPFPF", "mPmPm")
                    .aisle("~IMO~", "~iSC~", "~T~T~", "~p~p~", "~U~U~", "~N~N~", "~l~l~", "~A~A~", "~R~R~", "~B~B~", "~c~c~", "~e~e~", "~F~F~", "~m~m~");
            final TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            final int maxTier = TJConfig.machines.disableLayersInJEI ? 4 : 15;
            for (int tier = TJConfig.machines.disableLayersInJEI ? 3 : 0; tier < maxTier; tier++) {
                infos[tier] = builder.where('S', this.getController(), WEST)
                        .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN))
                        .where('M', GATileEntities.MAINTENANCE_HATCH[0], WEST)
                        .where('E', this.getEnergyHatch(tier, false), EAST)
                        .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[tier], WEST)
                        .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[tier], WEST)
                        .where('i', MetaTileEntities.ITEM_IMPORT_BUS[tier], WEST)
                        .where('o', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE))
                        .where('T', MetaBlocks.FRAMES.get(Materials.Thorium).getDefaultState())
                        .where('p', MetaBlocks.FRAMES.get(GAMaterials.Protactinium.getMaterial()).getDefaultState())
                        .where('U', MetaBlocks.FRAMES.get(GAMaterials.UraniumRadioactive.getMaterial()).getDefaultState())
                        .where('N', MetaBlocks.FRAMES.get(GAMaterials.Neptunium.getMaterial()).getDefaultState())
                        .where('l', MetaBlocks.FRAMES.get(GAMaterials.PlutoniumRadioactive.getMaterial()).getDefaultState())
                        .where('A', MetaBlocks.FRAMES.get(GAMaterials.AmericiumRadioactive.getMaterial()).getDefaultState())
                        .where('R', MetaBlocks.FRAMES.get(GAMaterials.Curium.getMaterial()).getDefaultState())
                        .where('B', MetaBlocks.FRAMES.get(GAMaterials.Berkelium.getMaterial()).getDefaultState())
                        .where('c', MetaBlocks.FRAMES.get(GAMaterials.Californium.getMaterial()).getDefaultState())
                        .where('e', MetaBlocks.FRAMES.get(GAMaterials.Einsteinium.getMaterial()).getDefaultState())
                        .where('F', MetaBlocks.FRAMES.get(GAMaterials.Fermium.getMaterial()).getDefaultState())
                        .where('m', MetaBlocks.FRAMES.get(GAMaterials.Mendelevium.getMaterial()).getDefaultState())
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN), new TextComponentTranslation("gregtech.multiblock.preview.limit", 5)
                .setStyle(new Style().setColor(TextFormatting.RED)));
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
