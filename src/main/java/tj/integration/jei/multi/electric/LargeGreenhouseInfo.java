package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregicadditions.machines.GATileEntities;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class LargeGreenhouseInfo extends TJMultiblockInfoPage {
    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_GREENHOUSE;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        GAMultiblockShapeInfo.Builder builder = GAMultiblockShapeInfo.builder(RIGHT, UP, BACK)
                .aisle("~CCCCC~", "~CCCCC~", "~CCCCC~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~~~~~~~")
                .aisle("CCCCCCC", "CDDDDDC", "C#####C", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("iCCCCCC", "IDDDDDC", "C#####C", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("MCCPCCC", "SDDDDDE", "C#####C", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("CCCCCCC", "ODDDDDC", "C#####C", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("CCCCCCC", "CDDDDDC", "C#####C", "G#####G", "G#####G", "G#####G", "G#####G", "~GGGGG~")
                .aisle("~CCCCC~", "~CCCCC~", "~CCCCC~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~GGGGG~", "~~~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN))
                .where('D', new BlockInfo(Block.getBlockFromName("randomthings:fertilizeddirt")))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST);
        for (int tier = 0; tier < 15; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[Math.min(9, tier)], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[Math.min(9, tier)], EnumFacing.WEST)
                    .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[Math.min(9, tier)], EnumFacing.WEST)
                    .where('P', GAMetaBlocks.PUMP_CASING.getState(PumpCasing.CasingType.values()[Math.max(0, tier -1)]))
                    .where('G', GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.values()[Math.min(6, tier)]))
                    .build());
        }
        return shapeInfos;
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
