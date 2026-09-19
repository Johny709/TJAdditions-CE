package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
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
import tj.machines.multi.parallel.MetaTileEntityParallelLargeCentrifuge;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;


public class ParallelLargeCentrifugeInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelLargeCentrifuge getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_CENTRIFUGE;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        final TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, RIGHT, DOWN);
        for (int layer = 0; layer < extent; layer++) {
            String entityS = layer == extent - 1 ? "~CSC~" : "~CGC~";
            builder.aisle("~CCC~", "CcccC", "CcmcC", "CcccC", "~CCC~");
            builder.aisle("CCCCC", "C###C", "C#P#C", "C###C", "CCCCC");
            builder.aisle(entityS, "C###C", "G#P#G", "C###C", "~CGC~");
            builder.aisle("CCCCC", "C###C", "C#P#C", "C###C", "CCCCC");
        }
        return builder.aisle("~IMO~", "CcccC", "CcmcC", "CcccC", "~iEo~").where('S', getController(), EnumFacing.WEST)
                    .where('C', GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.RED_STEEL))
                    .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                    .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE))
                    .where('c', PlaceholderType.COIL)
                    .where('m', PlaceholderType.MOTOR , GAMetaBlocks.MOTOR_CASING.getState(MotorCasing.CasingType.values()[0]))
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                    .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                    .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], EnumFacing.EAST)
                    .where('o', PlaceholderType.OUTPUT_HATCH ,MetaTileEntities.FLUID_EXPORT_HATCH[0], EnumFacing.EAST)
                    .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                    .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                    .build();
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_2.getItemVariant(MetalCasing2.CasingType.RED_STEEL), new TextComponentTranslation("gregtech.multiblock.preview.limit", 26)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.MOTOR_CASING.getItemVariant(MotorCasing.CasingType.MOTOR_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_large_centrifuge.description"),
                I18n.format("tj.multiblock.parallel.description"),
                I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.2").replace("§7", "§0").replace("§r", "§r§0")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
