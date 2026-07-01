package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJConfig;
import tj.integration.jei.TJMultiblockInfoPage;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.machines.TJMetaTileEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class LargeElectricImplosionCompressorInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_ELECTRIC_IMPLOSION_COMPRESSOR;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~", "CCCCC", "CCECC", "CCCCC", "~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~")
                .aisle("CCCCC", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "C#C#C", "C###C", "C###C", "C###C", "C#C#C", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "CCCCC")
                .aisle("~CCC~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~C#C~", "C###C", "C###C", "C###C", "~C#C~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~GPG~", "~CCC~")
                .aisle("CCCCC", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "C#C#C", "C###C", "C###C", "C###C", "C#C#C", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "GCGCG", "CCCCC")
                .aisle("~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~", "CCCCC", "CISOC", "CCMCC", "~C~C~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~G~G~", "~C~C~")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.EAST)
                .where('C', GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.INCOLOY_MA956))
                .where('G', MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING))
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                .where('P', PlaceholderType.PISTON, GAMetaBlocks.PISTON_CASING.getState(PistonCasing.CasingType.values()[0]))
                .build();

    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        Arrays.stream(PistonCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.PISTON_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
    }

    @Override
    public float getDefaultZoom() {
        return 0.5F;
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.large_electric_implosion_compressor.description").replace("§7", "§r")},
                super.getDescription());
    }
}
