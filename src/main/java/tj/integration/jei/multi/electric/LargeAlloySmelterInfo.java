package tj.integration.jei.multi.electric;

import gregicadditions.item.GAHeatingCoil;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.jei.GAMultiblockShapeInfo;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.multiblock.BlockPattern;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
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

import static gregicadditions.item.GAMetaBlocks.METAL_CASING_1;

public class LargeAlloySmelterInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_ALLOY_SMELTER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = GAMultiblockShapeInfo.builder(BlockPattern.RelativeDirection.RIGHT, BlockPattern.RelativeDirection.UP, BlockPattern.RelativeDirection.BACK)
                .aisle("CCCCC", "CCCCC", "CcccC", "#####")
                .aisle("CCCCC", "IcccC", "C###C", "CcccC")
                .aisle("CCCCC", "SCCCE", "McccC", "#####")
                .aisle("CCCCC", "OcccC", "C###C", "CcccC")
                .aisle("CCCCC", "CCCCC", "CcccC", "#####")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', METAL_CASING_1.getState(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST);
        for (int tier = 0; tier < 15; tier++) {
            shapeInfos.add(builder.where('E', this.getEnergyHatch(tier, false), EnumFacing.EAST)
                    .where('I', MetaTileEntities.ITEM_IMPORT_BUS[tier], EnumFacing.WEST)
                    .where('O', MetaTileEntities.ITEM_EXPORT_BUS[tier], EnumFacing.WEST)
                    .where('c', this.getCoils(tier))
                    .build());
        }
        return shapeInfos;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.ZIRCONIUM_CARBIDE), new TextComponentTranslation("gregtech.multiblock.preview.limit", 15)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        Arrays.stream(GAHeatingCoil.CoilType.values()).forEach(coilType -> this.addBlockTooltip(GAMetaBlocks.HEATING_COIL.getItemVariant(coilType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return new String[] {I18n.format("tj.multiblock.large_alloy_smelter.description").replace("§7", "§r")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
