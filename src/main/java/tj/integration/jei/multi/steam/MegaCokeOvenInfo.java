package tj.integration.jei.multi.steam;

import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import tj.machines.TJMetaTileEntities;

import java.util.List;

public class MegaCokeOvenInfo extends MultiblockInfoPage {
    public  MegaCokeOvenInfo() {
    }
    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.MEGA_COKE_OVEN;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "I#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "SFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "O#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "WFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF", "F#F#F#F#F", "FFFFFFFFF")
                .aisle("FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF", "FFFFFFFFF")
                .where('S', TJMetaTileEntities.MEGA_COKE_OVEN, EnumFacing.WEST)
                .where('F', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.COKE_BRICKS))
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.ULV], EnumFacing.WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.ULV], EnumFacing.WEST)
                .where('W', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.ULV], EnumFacing.WEST)
                .build();
        return Lists.newArrayList(shapeInfo);
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.COKE_BRICKS), new TextComponentTranslation("gregtech.multiblock.preview.limit", 100)
                .setStyle(new Style().setColor(TextFormatting.RED)));
    }


    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.default.description")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.3f;
    }
}
