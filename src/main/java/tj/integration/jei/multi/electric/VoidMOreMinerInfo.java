package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.integration.jei.TJMultiblockInfoPage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gregicadditions.GAMaterials.QCDMatter;
import static tj.machines.TJMetaTileEntities.VOID_MORE_MINER;

public class VoidMOreMinerInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return VOID_MORE_MINER;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo.Builder shapeInfo = MultiblockShapeInfo.builder()
                .aisle("CCCCCCCCC", "CCCCCCCCC", "C#######C", "C#######C", "C#######C", "CCCCCCCCC", "CFFFFFFFC", "CFFFFFFFC", "C#######C", "C#######C")
                .aisle("C#######C", "C#######C", "#########", "#########", "#########", "C###D###C", "F##DDD##F", "F##DDD##F", "###DDD###", "#########")
                .aisle("C#######C", "C#######C", "#########", "####D####", "###DDD###", "C##DDD##C", "F#DD#DD#F", "F#D###D#F", "##D###D##", "#########")
                .aisle("C###D###C", "I###D###C", "###DDD###", "###D#D###", "##DD#DD##", "C#D###D#C", "FDD###DDF", "FD#####DF", "#D#####D#", "#########")
                .aisle("M##DmD##E", "S##DmD##C", "###DmD###", "##D###D##", "##D###D##", "CDD###DDC", "FD#####DF", "FD#####DF", "#D#####D#", "#########")
                .aisle("o###D###C", "O###D###C", "###DDD###", "###D#D###", "##DD#DD##", "C#D###D#C", "FDD###DDF", "FD#####DF", "#D#####D#", "#########")
                .aisle("C#######C", "C#######C", "#########", "####D####", "###DDD###", "C##DDD##C", "F#DD#DD#F", "F#D###D#F", "##D###D##", "#########")
                .aisle("C#######C", "C#######C", "#########", "#########", "#########", "C###D###C", "F##DDD##F", "F##DDD##F", "###DDD###", "#########")
                .aisle("CCCCCCCCC", "CCCCCCCCC", "C#######C", "C#######C", "C#######C", "CCCCCCCCC", "CFFFFFFFC", "CFFFFFFFC", "C#######C", "C#######C")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.HEAVY_QUARK_DEGENERATE_MATTER))
                .where('D', TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.PERIODICIUM))
                .where('F', MetaBlocks.FRAMES.get(QCDMatter).getDefaultState())
                .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.UV], EnumFacing.WEST)
                .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.UV], EnumFacing.WEST)
                .where('o', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.UV], EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.WEST)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.UV], EnumFacing.EAST);
        return Arrays.stream(MotorCasing.CasingType.values())
                .map(casingType -> shapeInfo.where('m', GAMetaBlocks.MOTOR_CASING.getState(casingType)).build())
                .collect(Collectors.toList());
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        this.addBlockTooltip(TJMetaBlocks.SOLID_CASING.getItemVariant(BlockSolidCasings.SolidCasingType.HEAVY_QUARK_DEGENERATE_MATTER), new TextComponentTranslation("gregtech.multiblock.preview.limit", 100)
                .setStyle(new Style().setColor(TextFormatting.RED)));
        Arrays.stream(MotorCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.MOTOR_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return new String[] {
                I18n.format("gtadditions.multiblock.void_miner.description.1"),
                I18n.format("gtadditions.multiblock.void_miner.description.2"),
                I18n.format("gtadditions.multiblock.void_miner.description.3"),
                I18n.format("gtadditions.multiblock.void_miner.description.4"),
                I18n.format("gtadditions.multiblock.void_miner.description.5"),
                I18n.format("gtadditions.multiblock.void_miner.description.6"),
                I18n.format("tj.multiblock.void_more_miner.description")};
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
