package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockTurbineCasing;
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
import tj.machines.multi.parallel.MetaTileEntityParallelPlasmaCondenser;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelPlasmaCondenserInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelPlasmaCondenser getController() {
        return TJMetaTileEntities.PARALLEL_PLASMA_CONDENSER;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {


        final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, RIGHT, DOWN);
        builder.aisle("~~~~~", "~CCC~", "~CCC~", "~CCC~", "~~~~~");
        for (int layer = 0; layer < extent; layer++) {

            String entityS = layer == extent - 1 ? "~ISO~" : "~CCC~";

            builder.aisle("~CCC~", "CG#GC", "C#T#C", "CG#GC", "~CCC~");
            builder.aisle(entityS, "CPTPC", "CTTTC", "CPTPC", "~CCC~");
        }
        return builder.aisle("~iMo~", "CG#GC", "C#T#C", "CG#GC", "~CEC~")
            .aisle("~~~~~", "~CCC~", "~CCC~", "~CCC~", "~~~~~").where('S', this.getController(), WEST)
                .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.HASTELLOY_N))
                .where('G', MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX))
                .where('T', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE))
                .where('P', PlaceholderType.PUMP,GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[0]))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], WEST)
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), WEST)
                .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], WEST)
                .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[0], WEST)
                .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], WEST)
                .build();

    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.HASTELLOY_N), new TextComponentTranslation("gregtech.multiblock.preview.limit", 12)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.PUMP_CASING.getItemVariant(PumpCasing.CasingType.PUMP_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_plasma_condenser.description"),
                I18n.format("tj.multiblock.parallel.description")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
