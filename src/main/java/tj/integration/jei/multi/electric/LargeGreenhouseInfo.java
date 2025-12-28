package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregicadditions.machines.GATileEntities;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.machines.TJMetaTileEntities;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class LargeGreenhouseInfo extends TJMultiblockInfoPage {
    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_GREENHOUSE;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        GATransparentCasing.CasingType[] glassType = GATransparentCasing.CasingType.values();
        GAMultiblockShapeInfo.Builder shapeInfo = GAMultiblockShapeInfo.builder(RIGHT, UP, BACK)
                .aisle("~CCCCC~", "~CCCCC~", "~CCCCC~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~~~~~~~")
                .aisle("CCCCCCC", "CDDDDDC", "C#####C", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("iCCCCCC", "IDDDDDC", "C#####C", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("MCCPCCC", "SDDDDDE", "C#####C", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("CCCCCCC", "ODDDDDC", "C#####C", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("CCCCCCC", "CDDDDDC", "C#####C", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("~CCCCC~", "~CCCCC~", "~CCCCC~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~~~~~~~")
                .where('S', TJMetaTileEntities.LARGE_GREENHOUSE, EnumFacing.WEST)
                .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN))
                .where('D', new BlockInfo(Block.getBlockFromName("randomthings:fertilizeddirt")))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.IV], EnumFacing.WEST)
                .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.IV], EnumFacing.WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.IV], EnumFacing.WEST);
        return Arrays.stream(PumpCasing.CasingType.values())
                .map(casingType -> shapeInfo.where('P', GAMetaBlocks.PUMP_CASING.getState(casingType))
                        .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(glassType[Math.min(glassType.length - 1, casingType.ordinal())]))
                        .where('E', this.getEnergyHatch(casingType.getTier(), false), EnumFacing.EAST)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN), new TextComponentTranslation("gregtech.multiblock.preview.limit", 25)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        Arrays.stream(PumpCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.PUMP_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(GATransparentCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.TRANSPARENT_CASING.getItemVariant(casingType), COMPONENT_TIER_ANY_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.large_greenhouse.description")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
