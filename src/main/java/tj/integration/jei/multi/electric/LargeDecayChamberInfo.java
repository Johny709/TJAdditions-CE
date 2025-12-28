package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.FieldGenCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.item.metal.NuclearCasing;
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

public class LargeDecayChamberInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_DECAY_CHAMBER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        NuclearCasing.CasingType[] rodTypes = NuclearCasing.CasingType.values();
        MultiblockShapeInfo.Builder shapeInfo = MultiblockShapeInfo.builder()
                .aisle("~~C~~", "~CCC~", "CCCCC", "~CCC~", "~~C~~")
                .aisle("~CCC~", "!###C", "C#F#C", "I###C", "~CCC~")
                .aisle("CCCCC", "C#F#C", "SFRFE", "M#F#C", "CCCCC")
                .aisle("~CCC~", "0###C", "C#F#C", "O###C", "~CCC~")
                .aisle("~~C~~", "~CCC~", "CCCCC", "~CCC~", "~~C~~")
                .where('S', TJMetaTileEntities.LARGE_DECAY_CHAMBER, EnumFacing.WEST)
                .where('C', GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.LEAD))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LuV], EnumFacing.WEST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LuV], EnumFacing.WEST)
                .where('!', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.LuV], EnumFacing.WEST)
                .where('0', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.LuV], EnumFacing.WEST);
        return Arrays.stream(FieldGenCasing.CasingType.values())
                .map(casingType -> shapeInfo.where('F', GAMetaBlocks.FIELD_GEN_CASING.getState(casingType))
                        .where('E', this.getEnergyHatch(casingType.getTier(), false), EnumFacing.EAST)
                        .where('R', GAMetaBlocks.NUCLEAR_CASING.getState(rodTypes[Math.min(rodTypes.length - 1, casingType.ordinal())]))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(GAMetaBlocks.METAL_CASING_2.getItemVariant(MetalCasing2.CasingType.LEAD), new TextComponentTranslation("gregtech.multiblock.preview.limit", 24)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        Arrays.stream(FieldGenCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.FIELD_GEN_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(NuclearCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.NUCLEAR_CASING.getItemVariant(casingType), COMPONENT_TIER_ANY_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("tj.multiblock.large_decay_chamber.description")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
