package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.machines.GATileEntities;
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
import tj.machines.multi.parallel.MetaTileEntityParallelLargeMixer;

import java.util.ArrayList;
import java.util.List;

import static gregicadditions.GAMaterials.Staballoy;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelLargeMixerInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelLargeMixer getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_MIXER;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {

        final TJMultiblockShapeInfo.Builder builder = new TJMultiblockShapeInfo.Builder(FRONT, RIGHT, DOWN);
        builder.aisle("~~F~~", "~~F~~", "FFFFF", "~~F~~", "~~F~~");
        for (int layer = 0; layer < extent; layer++) {

            String entityS = layer == extent - 1 ? "~ISO~" : "~CCC~";

            builder.aisle("~CCC~", "C###C", "C#G#C", "C###C", "~CCC~");
            builder.aisle("~CCC~", "C###C", "C#m#C", "C###C", "~CCC~");
            builder.aisle(entityS, "C###C", "C#m#C", "C###C", "~CCC~");
        }
        return builder.aisle("~iMo~", "CCCCC", "CCCCC", "CCCCC", "~CEC~").where('S', this.getController(), WEST)
                .where('C', GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.STABALLOY))
                .where('G', GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.TUNGSTENSTEEL_GEARBOX_CASING))
                .where('F', MetaBlocks.FRAMES.get(Staballoy).getDefaultState())
                .where('M', PlaceholderType.MOTOR, GAMetaBlocks.MOTOR_CASING.getState(MotorCasing.CasingType.values()[0]))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], WEST)
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), WEST)
                .where('I', PlaceholderType.INPUT_BUS, MetaTileEntities.ITEM_IMPORT_BUS[0], WEST)
                .where('i', PlaceholderType.INPUT_HATCH, MetaTileEntities.FLUID_IMPORT_HATCH[0], WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[0], WEST)
                .where('o', PlaceholderType.OUTPUT_HATCH, MetaTileEntities.FLUID_EXPORT_HATCH[0], WEST)
                .build();
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_2.getItemVariant(MetalCasing2.CasingType.STABALLOY), new TextComponentTranslation("gregtech.multiblock.preview.limit", 20)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.MOTOR_CASING.getItemVariant(MotorCasing.CasingType.MOTOR_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_large_mixer.description"),
                I18n.format("tj.multiblock.parallel.description")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
