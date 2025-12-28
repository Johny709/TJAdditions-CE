package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.item.components.FieldGenCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import tj.integration.jei.TJMultiblockInfoPage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static tj.machines.TJMetaTileEntities.TELEPORTER;

public class TeleporterInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TELEPORTER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo.Builder shapeInfo = MultiblockShapeInfo.builder()
                .aisle("~CFC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("CCCCC", "~CCC~", "~C#C~", "~C#C~", "~C#C~", "~CCC~", "~~C~~")
                .aisle("FCfCF", "~ISE~", "~###~", "~###~", "~###~", "~MFC~", "~CfC~")
                .aisle("CCCCC", "~CCC~", "~C#C~", "~C#C~", "~C#C~", "~CCC~", "~~C~~")
                .aisle("~CFC~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING_MK2))
                .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.LV], EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST);
        return Arrays.stream(FieldGenCasing.CasingType.values())
                .map(casingType -> shapeInfo.where('f', GAMetaBlocks.FIELD_GEN_CASING.getState(casingType))
                        .where('E', this.getEnergyHatch(casingType.getTier(), false), EnumFacing.EAST)
                        .where('F', this.getVoltageCasing(casingType.getTier()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        Arrays.stream(FieldGenCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.FIELD_GEN_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(GAMultiblockCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MUTLIBLOCK_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(GAMultiblockCasing2.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MUTLIBLOCK_CASING2.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.teleporter.description")};
    }
}
