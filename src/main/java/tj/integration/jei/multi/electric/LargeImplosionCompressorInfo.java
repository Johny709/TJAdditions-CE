package tj.integration.jei.multi.electric;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
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
import static gregtech.api.unification.material.Materials.Steel;

public class LargeImplosionCompressorInfo extends TJMultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return TJMetaTileEntities.LARGE_IMPLOSION_COMPRESSOR;
    }

    @Override
    public MultiblockShapeInfo getMatchingShapes(int extent) {
        return TJMultiblockShapeInfo.builder(FRONT, UP, LEFT)
                .aisle("CCEMC", "F~~~F", "F~~~F", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .aisle("CCCCC", "~PGP~", "~PGP~", "~PGP~", "~PGP~", "~PGP~", "CCCCC")
                .aisle("CCpCC", "~G#G~", "~G#G~", "~G#G~", "~G#G~", "~G#G~", "CCmCC")
                .aisle("CCCCC", "~PGP~", "~PGP~", "~PGP~", "~PGP~", "~PGP~", "CCCCC")
                .aisle("CISOC", "F~~~F", "F~~~F", "F~~~F", "F~~~F", "F~~~F", "CCCCC")
                .where('S', this.getController(), EnumFacing.WEST)
                .where('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                .where('F', MetaBlocks.FRAMES.get(Steel).getDefaultState())
                .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE))
                .where('M', GATileEntities.MAINTENANCE_HATCH[0], EnumFacing.EAST)
                .where('m', GATileEntities.MUFFLER_HATCH[0], EnumFacing.UP)
                .where('E', PlaceholderType.ENERGY_INPUT_HATCH, this.getEnergyHatch(0, false), EnumFacing.EAST)
                    .where('I', PlaceholderType.INPUT_BUS , MetaTileEntities.ITEM_IMPORT_BUS[0], EnumFacing.WEST)
                    .where('O', PlaceholderType.OUTPUT_BUS, MetaTileEntities.ITEM_EXPORT_BUS[0], EnumFacing.WEST)
                    .where('G', PlaceholderType.GLASS, GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.values()[0]))
                    .where('P', PlaceholderType.PISTON, GAMetaBlocks.PISTON_CASING.getState(PistonCasing.CasingType.values()[0]))
                    .build();
    }

    @Override
    public float getDefaultZoom() {
        return 0.75F;
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        Arrays.stream(PistonCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.PISTON_CASING.getItemVariant(casingType), COMPONENT_BLOCK_TOOLTIP));
        Arrays.stream(GATransparentCasing.CasingType.values()).forEach(casingType -> this.addBlockTooltip(GAMetaBlocks.TRANSPARENT_CASING.getItemVariant(casingType), COMPONENT_TIER_ANY_TOOLTIP));
    }

    @Override
    public String[] getDescription() {
        return ArrayUtils.addAll(new String[]{I18n.format("tj.multiblock.large_implosion_compressor.description").replace("§7", "§r")},
                super.getDescription());
    }
}
