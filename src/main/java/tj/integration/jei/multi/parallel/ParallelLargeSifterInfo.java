package tj.integration.jei.multi.parallel;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
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

import static gregicadditions.GAMaterials.EglinSteel;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.WEST;


public class ParallelLargeSifterInfo extends TJMultiblockInfoPage implements IParallelMultiblockInfoPage {

    @Override
    public ParallelRecipeMapMultiblockController getController() {
        return TJMetaTileEntities.PARALLEL_LARGE_SIFTER;
    }

    @Override
    public List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes) {
        List<TJMultiblockShapeInfo[]> shapeInfos = new ArrayList<>();
        int size = Math.min(TJConfig.machines.maxLayersInJEI, this.getController().getMaxParallel());
        for (int shapeInfo = 1; shapeInfo <= size; shapeInfo++) {
            TJMultiblockShapeInfo.Builder builder = TJMultiblockShapeInfo.builder(FRONT, RIGHT, DOWN);
            for (int layer = 1; layer < shapeInfo; layer++) {
                builder.aisle("~CCC~", "C###C", "C###C", "C###C", "~CCC~");
                builder.aisle("CCCCC", "PGGGP", "CGGGC", "PGGGP", "CCCCC");
                builder.aisle("~CCC~", "C###C", "C###C", "C###C", "~CCC~");
                builder.aisle("~FCF~", "F###F", "C###C", "F###F", "~FCF~");
            }
            builder.aisle("~CCC~", "C###C", "C###C", "C###C", "~CCC~")
                    .aisle("CISOC", "PGGGP", "CGGGC", "PGGGP", "CCECC")
                    .aisle("~CMC~", "C###C", "C###C", "C###C", "~CCC~")
                    .aisle("~C~C~", "CCCCC", "~C~C~", "CCCCC", "~C~C~")
                    .aisle("~C~C~", "CCCCC", "~C~C~", "CCCCC", "~C~C~");
            TJMultiblockShapeInfo[] infos = new TJMultiblockShapeInfo[15];
            for (int tier = 0; tier < infos.length; tier++) {
                infos[tier] = builder.where('S', getController(), WEST)
                        .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.EGLIN_STEEL))
                        .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                        .where('F', MetaBlocks.FRAMES.get(EglinSteel).getDefaultState())
                        .where('P', GAMetaBlocks.PISTON_CASING.getState(PistonCasing.CasingType.values()[Math.max(0, tier - 1)]))
                        .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], WEST)
                        .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], WEST)
                        .where('E', this.getEnergyHatch(tier, false), EAST)
                        .where('M', GATileEntities.MAINTENANCE_HATCH[0], WEST)
                        .build();
            }
            shapeInfos.add(infos);
        }
        return shapeInfos;
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
                I18n.format("tj.multiblock.parallel.description"),
                I18n.format("tj.multiblock.parallel.extend.tooltip")},
                super.getDescription());
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}

