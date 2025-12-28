package tj.integration.jei.multi.steam;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
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

import static gregicadditions.item.GAMetaBlocks.METAL_CASING_1;

public class IndustrialSteamEngineInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.INDUSTRIAL_STEAM_ENGINE;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo.Builder shapeInfo = MultiblockShapeInfo.builder()
                .aisle("~~C~", "~CCC", "~~C~")
                .aisle("mCCC", "SFRE", "ICCO")
                .aisle("CCCC", "CCCC", "CCCC")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('m', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.TUMBAGA))
                .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.MV], EnumFacing.WEST)
                .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.MV], EnumFacing.EAST);
        return Arrays.stream(MotorCasing.CasingType.values())
                .map(casingType -> shapeInfo.where('R', GAMetaBlocks.MOTOR_CASING.getState(casingType))
                        .where('F', this.getVoltageCasing(casingType.getTier()))
                        .where('E', this.getEnergyHatch(casingType.getTier(), true), EnumFacing.EAST)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.TUMBAGA), new TextComponentTranslation("gregtech.multiblock.preview.limit", 8)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        Arrays.stream(MotorCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MOTOR_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(GAMultiblockCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MUTLIBLOCK_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(GAMultiblockCasing2.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MUTLIBLOCK_CASING2.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.industrial_steam_engine.description")};
    }
}
