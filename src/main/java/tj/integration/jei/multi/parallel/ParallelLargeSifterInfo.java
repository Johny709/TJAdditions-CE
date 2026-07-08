package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockMultiblockCasing;
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
import tj.machines.multi.parallel.MetaTileEntityParallelLargeSifter;

import java.util.ArrayList;
import java.util.List;

import static gregicadditions.GAMaterials.EglinSteel;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelLargeSifterInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public MetaTileEntityParallelLargeSifter getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_SIFTER;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        final TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, RIGHT, DOWN);
        for (int layer = 1; layer < extent; layer++) {
            builder.aisle("~CCC~", "C###C", "C###C", "C###C", "~CCC~");
            builder.aisle("CCCCC", "PGGGP", "CGGGC", "PGGGP", "CCCCC");
            builder.aisle("~CCC~", "C###C", "C###C", "C###C", "~CCC~");
            builder.aisle("~FCF~", "F###F", "C###C", "F###F", "~FCF~");
        }
        return builder.aisle("~CCC~", "C###C", "C###C", "C###C", "~CCC~")
                .aisle("CISOC", "PGGGP", "CGGGC", "PGGGP", "CCECC")
                .aisle("~CMC~", "C###C", "C###C", "C###C", "~CCC~")
                .aisle("~C~C~", "CCCCC", "~C~C~", "CCCCC", "~C~C~")
                .aisle("~C~C~", "CCCCC", "~C~C~", "CCCCC", "~C~C~").where('S', getController(), WEST)
                    .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.EGLIN_STEEL))
                    .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                    .where('F', MetaBlocks.FRAMES.get(EglinSteel).getDefaultState())
                    .where('P', PlaceholderType.PISTON, GAMetaBlocks.PISTON_CASING.getState(PistonCasing.CasingType.values()[0]))
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[0], WEST)
                    .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EAST)
                    .where('M', GATileEntities.MAINTENANCE_HATCH[0], WEST)
                    .build();
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.EGLIN_STEEL), new TextComponentTranslation("gregtech.multiblock.preview.limit", 40)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        this.addBlockTooltip(GAMetaBlocks.PISTON_CASING.getItemVariant(PistonCasing.CasingType.PISTON_LV), COMPONENT_BLOCK_TOOLTIP);
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[] {
                I18n.format("tj.multiblock.parallel_large_sifter.description"),
                I18n.format("tj.multiblock.parallel.description")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}

